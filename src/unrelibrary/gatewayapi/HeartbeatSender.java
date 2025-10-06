package unrelibrary.gatewayapi;

import unrelibrary.APICommunicationManager;
import unrelibrary.formatting.GeneralFormatter;
import unrelibrary.formatting.JSONBuilder;
import unrelibrary.formatting.JSONRepresentable;

// you need to send heartbeats in regular intervals via tha gateway, otherwise you will be disconnected.
public class HeartbeatSender implements Runnable {
    private final int heartbeatInterval;
    private final APICommunicationManager apiCommunicationManager; // save the reference for closing it if a heartbeat wasn't acked, for exchanging codes, and also for shutting down
    private final CustomGatewayListener customGatewayListener; // these two communicate occasionally
    private final boolean VERBOSE;

    private final long TIME_FOR_ACKING = 5000; // in milliseconds discord needs to ack every heartbeat; if it doesn't, we need to close the connection.
    private long lastHeartbeatTime = -1;
    private boolean lastHeartbeatACKed = true; // consider the 0th heartbeat acked.
    private long currentTime;
    private long nextHeartbeatTime;

    public HeartbeatSender(int heartbeatInterval, APICommunicationManager apiCommunicationManager, CustomGatewayListener customGatewayListener, boolean verbose) {
        this.heartbeatInterval = heartbeatInterval;
        this.apiCommunicationManager = apiCommunicationManager;
        this.customGatewayListener = customGatewayListener;
        this.VERBOSE = verbose;
    }

    private class Heartbeat implements JSONRepresentable {
        private final Integer SEQUENCE_NUMBER;
        
        private Heartbeat(Integer sequenceNumber) {
            this.SEQUENCE_NUMBER = sequenceNumber;
        }

        public String toJSON() {
            JSONBuilder toReturn = new JSONBuilder();
            toReturn.addIntProperty("op", 1); // 1 is for heartbeats
            toReturn.addIntProperty("d", SEQUENCE_NUMBER);
            return toReturn.build();
        }
    }

    public void sendHeartbeat() {
        Integer sequenceNumber = apiCommunicationManager.gatewayManager.lastSequenceNumber;
        Heartbeat heartbeat = new Heartbeat(sequenceNumber);
        String heartbeatString = heartbeat.toJSON();
        apiCommunicationManager.gatewayManager.webSocket.sendText(heartbeatString, true);
        lastHeartbeatTime = currentTime;
        nextHeartbeatTime = currentTime + heartbeatInterval;
        lastHeartbeatACKed = false;
        return;
    }

    public void run() {
        GeneralFormatter.printlnIfVerbose("Started hearbeatsender thread.", VERBOSE);
        // use system time for more accuracy than putting the thread to sleep
        currentTime = System.currentTimeMillis();
        long randomizedInterval = (long) (Math.random() * heartbeatInterval); // we need to put a random offset on our first beat
        nextHeartbeatTime = currentTime + randomizedInterval;
        lastHeartbeatTime = -1; // -1 means none has been sent yet
        GeneralFormatter.printlnIfVerbose("Initial heartbeat offset:" + randomizedInterval, VERBOSE);
        while (!Thread.interrupted()) {
            try {
                Thread.sleep(1);
                currentTime = System.currentTimeMillis();
                if (!lastHeartbeatACKed && currentTime >= lastHeartbeatTime + TIME_FOR_ACKING) {
                    if (customGatewayListener.getLastHeartbeatAckTime() >= lastHeartbeatTime) {
                        lastHeartbeatACKed = true;
                    } else {
                        GeneralFormatter.printlnIfVerbose("A heartbeat wasn't acked. Attempting to reconnect...", VERBOSE);
                        apiCommunicationManager.gatewayManager.reconnect();
                    }
                }
                if (currentTime >= nextHeartbeatTime) {
                    sendHeartbeat();
                }
                if (customGatewayListener.sendHeartbeatImmediately) {
                    sendHeartbeat();
                    customGatewayListener.sendHeartbeatImmediately = false;
                }
            } catch (InterruptedException e) {
                // this exception is thrown when the thread is supposed to be interrupted but is currently sleeping
                // since this loop is meant to only run while the thread isnt interrupted anyway, we can just break
                break;
            }
        }
        GeneralFormatter.printlnIfVerbose("Shutting down HeartbeatSender...", VERBOSE);
        return;
    }
}