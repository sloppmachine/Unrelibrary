package unrelibrary.discordobjects.events;

import unrelibrary.formatting.JSONBuilder;
import unrelibrary.formatting.JSONRepresentable;

// this is an event with opcode 2.
public class GatewayIdentifyEvent extends GatewayEvent {
    public final Data DATA;
    
    public GatewayIdentifyEvent(Integer s, String t, Data data) {
        super(2, s, t);
        this.DATA = data;
    }
    
    // https://discord.com/developers/docs/events/gateway-events#hello-hello-structure
    public static class Data implements JSONRepresentable {
        public final String TOKEN;
        public final int INTENTS;
        public final LocalProperties PROPERTIES;

        public Data(String token, int intents, LocalProperties properties) {
            this.TOKEN = token;
            this.INTENTS = intents;
            this.PROPERTIES = properties;
        }

        // https://discord.com/developers/docs/events/gateway-events#identify-identify-connection-properties
        public static class LocalProperties implements JSONRepresentable {
            public final String OS;
            public final String BROWSER;
            public final String DEVICE;

            public LocalProperties(String os, String browser, String device) {
                this.OS = os;
                this.BROWSER = os;
                this.DEVICE = device;
            }

            public String toJSON() {
                JSONBuilder toReturnBuilder = new JSONBuilder();
                toReturnBuilder.addStringProperty("os", OS);
                toReturnBuilder.addStringProperty("browser", BROWSER);
                toReturnBuilder.addStringProperty("device", DEVICE);
                return toReturnBuilder.build();
            }
        }

        public String toJSON() {
            JSONBuilder toReturnBuilder = new JSONBuilder();
            toReturnBuilder.addStringProperty("token", TOKEN);
            toReturnBuilder.addIntProperty("intents", INTENTS);
            toReturnBuilder.addLiteralProperty("properties", PROPERTIES.toJSON());
            return toReturnBuilder.build();
        }
    }

    public String toJSON() {
        JSONBuilder toReturnBuilder = new JSONBuilder();
        toReturnBuilder.addIntProperty("op", OP);
        toReturnBuilder.addLiteralProperty("d", DATA.toJSON());
        return toReturnBuilder.build();
    }
}
