package unrelibrary.discordobjects;

// special pack of data discord sends after successfully connecting with gateway
// https://discord.com/developers/docs/events/gateway-events#identify-identify-connection-properties
public class GatewayReadyEventData {
    public final String SESSION_ID;
    public final String RESUME_GATEWAY_URL;

    public GatewayReadyEventData(String sessionID, String resumeGatewayURL) {
        this.SESSION_ID = sessionID;
        this.RESUME_GATEWAY_URL = resumeGatewayURL;
    }
}
