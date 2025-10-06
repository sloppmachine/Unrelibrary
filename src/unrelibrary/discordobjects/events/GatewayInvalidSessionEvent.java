package unrelibrary.discordobjects.events;

// send when the gateway session has become invalid.
// https://discord.com/developers/docs/events/gateway-events#invalid-session
public class GatewayInvalidSessionEvent extends GatewayEvent {
    public final boolean D;

    public GatewayInvalidSessionEvent(Integer s, String t, boolean d) {
        super(9, s, t);
        this.D = d;
    }
}
