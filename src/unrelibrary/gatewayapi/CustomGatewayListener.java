package unrelibrary.gatewayapi;

import java.util.concurrent.CompletionStage;
import java.net.http.WebSocket;
import java.net.http.WebSocket.Listener;

import unrelibrary.APICommunicationManager;
import unrelibrary.discordobjects.Message;
import unrelibrary.discordobjects.events.GatewayDispatchEvent;
import unrelibrary.discordobjects.events.GatewayEvent;
import unrelibrary.discordobjects.events.GatewayHelloEvent;
import unrelibrary.discordobjects.events.GatewayInvalidSessionEvent;
import unrelibrary.formatting.GeneralFormatter;
import unrelibrary.formatting.JSONBuilder;
import unrelibrary.formatting.JSONFactory;


// this class is built for listening to the gateway api
public class CustomGatewayListener implements Listener {
    private final boolean FIRST_CONNECT; // dont identify if you're reconnecting
    public final APICommunicationManager apiCommunicationManager; // save a reference for starting the heartbeater thread
    private final boolean VERBOSE;
    private final EventReceiver EVENT_RECEIVER;

    private boolean heartbeatsSetUp = false;
    private long lastHeartbeatACKTime = -1; // can be read by getLastHeartbeatAckTime. important for HeartbeatSender
    private String receivedUntilNow = ""; // some messages come in multiple parts.
    public boolean sendHeartbeatImmediately = false; // this is also read and written by HeartbeatSender

    public CustomGatewayListener(APICommunicationManager apiCommunicationManager, boolean firstConnect, boolean verbose, EventReceiver eventReceiver) {
        this.apiCommunicationManager = apiCommunicationManager;
        this.FIRST_CONNECT = firstConnect;
        this.VERBOSE = verbose;
        this.EVENT_RECEIVER = eventReceiver;
    }

    public long getLastHeartbeatAckTime() {
        return lastHeartbeatACKTime;
    }

    // these methods have a default implementation, therefore use @Override
    @Override
    public void onOpen(WebSocket webSocket) {
        GeneralFormatter.printlnIfVerbose("Opened the Websocket.", VERBOSE);
        // this means that the websocket still has 1 more request that it's still supposed to answer
        // if the number of requests that it's still supposed to answer reaches 0, none of these methods are even called
        webSocket.request(1);
        return;
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        if (last) {
            data = receivedUntilNow + data;
            receivedUntilNow = "";
            GatewayEvent gatewayEvent = JSONFactory.jsonToDiscordGatewayEvent(data.toString());
            apiCommunicationManager.gatewayManager.lastSequenceNumber = gatewayEvent.S;
            // see opcodes for receiving: https://discord.com/developers/docs/topics/opcodes-and-status-codes#gateway-gateway-opcodes
            if (gatewayEvent.OP == 0) {
                // this is a dispatch event (usually, something happened on discord)
                // the data will be in the d
                GatewayDispatchEvent receivedEvent = JSONFactory.jsonToDiscordGatewayDispatchEvent(data.toString());
                if (receivedEvent.S != null) {
                    apiCommunicationManager.gatewayManager.lastDispatchSequenceNumber = receivedEvent.S;
                }
                
                // very sloppy solution, but isn't that what we're here for?
                if (receivedEvent.T.equals("READY")) {
                    apiCommunicationManager.gatewayManager.extractReadyEventData(JSONFactory.extractReadyEvent(receivedEvent.RECEIVED_DATA.RAW));
                } else if (receivedEvent.T.equals("MESSAGE_CREATE")) {
                    Message message = JSONFactory.jsonToDiscordMessage(receivedEvent.RECEIVED_DATA.RAW);
                    if (!apiCommunicationManager.gatewayManager.newMessageChannels.contains(message.CHANNEL_ID)
                        && message.USER.ID != apiCommunicationManager.getOwnID()) {
                        apiCommunicationManager.gatewayManager.newMessageChannels.add(message.CHANNEL_ID);
                    }
                    EVENT_RECEIVER.ON_MESSAGE_CREATE.accept(message, apiCommunicationManager);
                }
            } else if (gatewayEvent.OP == 1) {
                // this means it's a heartbeat. we need to send a heartbeat immediately.
                sendHeartbeatImmediately = true;
            } else if (gatewayEvent.OP == 7) {
                // this means we need to reconnect
                GeneralFormatter.printlnIfVerbose("Got a request to reconnect.", VERBOSE);
                apiCommunicationManager.gatewayManager.reconnect();
            } else if (gatewayEvent.OP == 9) {
                GatewayInvalidSessionEvent receivedEvent = JSONFactory.jsonToDiscordGatewayInvalidSessionEvent(data.toString());
                if (receivedEvent.D) {
                    GeneralFormatter.printlnIfVerbose("The session was invalidated, but reconnecting is possible. Reconnecting...", VERBOSE);
                    apiCommunicationManager.gatewayManager.reconnect();
                } else {
                    GeneralFormatter.printlnIfVerbose("The session was invalidated, but reconnecting will have to establish an entirely new connection.", VERBOSE);
                    apiCommunicationManager.gatewayManager.reconnectable = false;
                }
            } else if (gatewayEvent.OP == 10) {
                // this means it's a "hello" event. we need to respond
                GatewayHelloEvent receivedHelloEvent = JSONFactory.jsonToDiscordGatewayHelloEvent(data.toString());
                if (!FIRST_CONNECT) {
                    JSONBuilder resumeEventBuilder = new JSONBuilder();
                    resumeEventBuilder.addIntProperty("op", 6);
                    JSONBuilder resumeEventDataBuilder = new JSONBuilder();
                    resumeEventDataBuilder.addStringProperty("token", apiCommunicationManager.gatewayManager.TOKEN);
                    resumeEventDataBuilder.addStringProperty("session_id", apiCommunicationManager.gatewayManager.getSessionID());
                    resumeEventDataBuilder.addIntProperty("seq", apiCommunicationManager.gatewayManager.lastDispatchSequenceNumber);
                    resumeEventBuilder.addLiteralProperty("d", resumeEventDataBuilder.build());
                    webSocket.sendText(resumeEventBuilder.build(), true);
                }
                apiCommunicationManager.gatewayManager.startHeartbeats(receivedHelloEvent.RECEIVED_DATA.HEARTBEAT_INTERVAL);
            } else if (gatewayEvent.OP == 11) {
                // a heartbeat was ACKed
                // heartbeat ACKs can't be attributed to a specific heartbeat
                lastHeartbeatACKTime = System.currentTimeMillis();
                // if this is the first heartbeat that was ACKed, we can identify
                if (!heartbeatsSetUp) {
                    heartbeatsSetUp = true;
                    apiCommunicationManager.gatewayConnected();
                    apiCommunicationManager.gatewayManager.connected = true;
                    if (FIRST_CONNECT) {
                        GeneralFormatter.printlnIfVerbose("Identifying.", VERBOSE);
                        apiCommunicationManager.gatewayManager.identify();
                    }
                }
            }
            webSocket.request(1);
            return null;
        } else {
            receivedUntilNow += data;
            webSocket.request(1);
            return null;
        }
    }

    @Override
    public void onError(WebSocket webSocket, Throwable error) {
        GeneralFormatter.printException("Some websocket error occured.", error);
        webSocket.request(1);
        return;
    }

    @Override
    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
        GeneralFormatter.printlnIfVerbose("The websocket got closed because of reason :" + reason + " (code " + statusCode + ").", VERBOSE);
        apiCommunicationManager.gatewayManager.connected = false;
        return null;
    }
}