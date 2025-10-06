package unrelibrary.discordobjects.events;

import unrelibrary.formatting.JSONBuilder;
import unrelibrary.formatting.JSONRepresentable;

// https://discord.com/developers/docs/events/gateway-events#hello
public class GatewayHelloEvent extends GatewayEvent {
    public final Data RECEIVED_DATA;

    public GatewayHelloEvent(Integer s, String t, Data receivedData) {
        super(10, s, t);
        this.RECEIVED_DATA = receivedData;
    }

    // the d field contains this: https://discord.com/developers/docs/events/gateway-events#hello-hello-structure    
    public static class Data implements JSONRepresentable {
        public final int HEARTBEAT_INTERVAL;

        public Data(int heartbeatInterval) {
            this.HEARTBEAT_INTERVAL = heartbeatInterval;
        }

        public String toJSON() {
            JSONBuilder toReturnBuilder = new JSONBuilder();
            toReturnBuilder.addIntProperty("heartbeatInterval", HEARTBEAT_INTERVAL);
            return toReturnBuilder.build();
        }
    }
}
