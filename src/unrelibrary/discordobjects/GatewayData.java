package unrelibrary.discordobjects;

import unrelibrary.formatting.JSONBuilder;
import unrelibrary.formatting.JSONRepresentable;

// discord imposes a limit on how much you can use the gateway.
// https://discord.com/developers/docs/events/gateway#connecting
public class GatewayData implements JSONRepresentable {
    public final String URL;
    public final SessionStartLimit SESSION_START_LIMIT;

    public GatewayData(String url, SessionStartLimit sessionStartLimit) {
        this.URL = url;
        this.SESSION_START_LIMIT = sessionStartLimit;
    }

    // https://discord.com/developers/docs/events/gateway#get-gateway-bot
    public static class SessionStartLimit {
        public final int TOTAL;
        public final int REMAINING;
        public final int RESET_AFTER;
        public final int MAX_CONCURRENCY;

        public SessionStartLimit(int total, int remaining, int resetAfter, int maxConcurrency) {
            this.TOTAL = total;
            this.REMAINING = remaining;
            this.RESET_AFTER = resetAfter;
            this.MAX_CONCURRENCY = maxConcurrency;
        }
    }

    public String toJSON() {
        JSONBuilder toReturnBuilder = new JSONBuilder();
        toReturnBuilder.addStringProperty("url", URL);
        return toReturnBuilder.build();
    }
}