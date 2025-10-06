package unrelibrary.discordobjects.events;

import unrelibrary.formatting.JSONBuilder;
import unrelibrary.formatting.JSONRepresentable;

// https://discord.com/developers/docs/events/gateway-events
public class GatewayEvent implements JSONRepresentable {
    public final int OP;
    public final Integer S;
    public final String T;

    public GatewayEvent(int op, Integer s, String t) {
        this.OP = op;
        this.S = s;
        this.T = t;
    }

    public String toJSON() {
        JSONBuilder toReturnBuilder = new JSONBuilder();
        toReturnBuilder.addIntProperty("op", OP);
        toReturnBuilder.addIntProperty("s", S);
        toReturnBuilder.addStringProperty("t", T);
        return toReturnBuilder.build();
    }
}
