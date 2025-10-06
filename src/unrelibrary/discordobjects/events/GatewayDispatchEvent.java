package unrelibrary.discordobjects.events;

import java.util.Map;

import unrelibrary.formatting.JSONBuilder;
import unrelibrary.formatting.JSONRepresentable;

// this class is used for any gateway event with opcode 0. these are (along with others) listed here: https://discord.com/developers/docs/events/gateway#connecting
public class GatewayDispatchEvent extends GatewayEvent {
    public final Data RECEIVED_DATA;

    public GatewayDispatchEvent(Integer s, String t, Data receivedData) {
        super(0, s, t);
        this.RECEIVED_DATA = receivedData;
    }

    public static class Data implements JSONRepresentable {
        public final String RAW;
        public final Map<String, String> ENTRIES;

        public Data(String raw, Map<String, String> entries) {
            this.RAW = raw;
            this.ENTRIES = entries;
        }        

        public String toJSON() {
            JSONBuilder toReturnBuilder = new JSONBuilder();
            for (Map.Entry<String, String> entry : ENTRIES.entrySet()) {
                toReturnBuilder.addLiteralProperty(entry.getKey(), entry.getValue());
            }
            return toReturnBuilder.build();
        }
    }
    
}
