package unrelibrary.gatewayapi;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.LinkedList;
import java.util.List;

import unrelibrary.APICommunicationManager;
import unrelibrary.discordobjects.GatewayData;
import unrelibrary.discordobjects.GatewayReadyEventData;
import unrelibrary.discordobjects.events.GatewayIdentifyEvent;
import unrelibrary.formatting.GeneralFormatter;
import unrelibrary.formatting.JSONFactory;
import unrelibrary.restapi.ServerResponseException;

public class GatewayManager {
    private final APICommunicationManager apiCommunicationManager;
    private final int API_VERSION;
    public final String TOKEN;
    private final boolean VERBOSE;
    private final EventReceiver EVENT_RECEIVER;
    
    private String wssURL = "";
    private String resumeGatewayURL = "";
    protected Integer lastSequenceNumber = null; // this is needed to respond to heartbeats. written and read by the other classes of this package.
    protected Integer lastDispatchSequenceNumber = null; // the same.
    protected boolean connected = false; // written to by CustomGatewayListener, readable with isConnected
    private boolean reconnecting = false;
    protected boolean reconnectable = true; // if this is false, do not try to reconnect.
    private CustomGatewayListener customGatewayListener;
    private Thread heartbeatSenderThread;
    private GatewayData gatewayData;
    private String sessionID; // can be read with getSessionID

    public WebSocket webSocket; // for later asynchronous reference from HeartbeatSender

    public List<Long> newMessageChannels; // meant to be read and written by outside classes, like manual control.

    public GatewayManager(APICommunicationManager apiCommunicationManager, int apiVersion, String token, boolean verbose, EventReceiver eventReceiver) {
        this.apiCommunicationManager = apiCommunicationManager;
        this.API_VERSION = apiVersion;
        this.TOKEN = token;
        VERBOSE = verbose;
        this.newMessageChannels = new LinkedList<Long>();
        this.connected = false;
        this.reconnecting = false;
        this.EVENT_RECEIVER = eventReceiver;
    }

    public boolean isConnected() {
        return connected;
    }

    public String getSessionID() {
        return sessionID;
    }

    // data about the gateway, besides the adress to connect to, includes the current information on rate limits.
    public void getGatewayData() throws ServerResponseException {
        GatewayData gatewayData = JSONFactory.jsonToDiscordGatewayData(apiCommunicationManager.restManager.getGatewayData());
        this.gatewayData = gatewayData;
        this.wssURL = gatewayData.URL + "/?v=" + API_VERSION + "&encoding=json";
        return;
    }

    public void extractReadyEventData(GatewayReadyEventData gatewayReadyEventData) {
        resumeGatewayURL = gatewayReadyEventData.RESUME_GATEWAY_URL + "/?v=" + API_VERSION + "&encoding=json";
        sessionID = gatewayReadyEventData.SESSION_ID;
        return;
    }

    public void printGatewayAllowance() {
        if (gatewayData == null) {
            GeneralFormatter.printlnIfVerbose("No gateway Data has been collected yet!", VERBOSE);
        } else {
            GeneralFormatter.printlnIfVerbose("Remaining websocket allowance: " + gatewayData.SESSION_START_LIMIT.REMAINING + " (resets every " + gatewayData.SESSION_START_LIMIT.RESET_AFTER + " ms)", VERBOSE);
            GeneralFormatter.printlnIfVerbose("Currently allowed requests per 5 seconds: " + gatewayData.SESSION_START_LIMIT.MAX_CONCURRENCY, VERBOSE);
            GeneralFormatter.printlnIfVerbose("URL: " + gatewayData.URL, VERBOSE);
        }
        return;
    }

    public void openWebsocket() {
        customGatewayListener = new CustomGatewayListener(apiCommunicationManager, true, VERBOSE, EVENT_RECEIVER);
        webSocket = HttpClient.newHttpClient().newWebSocketBuilder().buildAsync(URI.create(wssURL), this.customGatewayListener).join();
        return;
    }

    public void closeWebsocket() {
        try {
            // since the websocket is a thread manager of it's own, we need to wait for it to be closed.
            webSocket.sendClose(1000, "User-requested shutdown").get();
        } catch (Exception exception) {
            GeneralFormatter.printException("The websocket was closed an in unexpected way.", exception);
        }
        return;
    }

    public void reconnect() {
        if (!reconnectable) {
            GeneralFormatter.printlnIfVerbose("Can't reconnect because the Gateway API stated not to reconnect. Starting from the beginning again instead", VERBOSE);
            apiCommunicationManager.disconnectGateway();
            apiCommunicationManager.setUpGateway();
            return;
        }
        if (!reconnecting) { // otherwise, the gateway listener would think the session was invalidated by discord and try to reconnect, making an infinite loop
            GeneralFormatter.printlnIfVerbose("Starting to reconnect...", VERBOSE);
            stopHeartbeats();
            
            connected = false;
            reconnecting = true;
            webSocket.sendClose(4000, "reconnecting");
            GeneralFormatter.printlnIfVerbose("Reconnecting to " + resumeGatewayURL + "...", VERBOSE);
            customGatewayListener = new CustomGatewayListener(apiCommunicationManager, false, VERBOSE, EVENT_RECEIVER);
            webSocket = HttpClient.newHttpClient().newWebSocketBuilder().buildAsync(URI.create(resumeGatewayURL), this.customGatewayListener).join();
            reconnecting = false;
            GeneralFormatter.printlnIfVerbose("Finished reconnecting.", VERBOSE);
        }
        return;
    }

    // you need to regularly maintain heartbeats while having an active connection to the gateway api
    public void startHeartbeats(int interval) {
        GeneralFormatter.printlnIfVerbose("Starting heartbeat: " + interval, VERBOSE);
        heartbeatSenderThread = new Thread(new HeartbeatSender(interval, apiCommunicationManager, customGatewayListener, VERBOSE));
        heartbeatSenderThread.start();
        return;
    }

    public void stopHeartbeats() {
        heartbeatSenderThread.interrupt();
        while (true) {
            try {
                heartbeatSenderThread.join();
                break;
            } catch (InterruptedException e) {
                // called if someone has interrupted the main thread. this will un-interrupt the main thread, so we can just try again
                continue;
            }
        }
        return;
    }

    public void identify() {
        GatewayIdentifyEvent gatewayIdentifyEvent = new GatewayIdentifyEvent(
            null,
            null,
            new GatewayIdentifyEvent.Data(
                TOKEN,
                (int) (Math.pow(2, 26) - 1),
                new GatewayIdentifyEvent.Data.LocalProperties(
                    "linux",
                    "unrelibrary",
                    "unrelibrary"
                )
            )
        );
        webSocket.sendText(gatewayIdentifyEvent.toJSON(), true);
        return;
    }
}
