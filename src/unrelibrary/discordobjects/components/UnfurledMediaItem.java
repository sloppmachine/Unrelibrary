package unrelibrary.discordobjects.components;

import unrelibrary.formatting.JSONBuilder;
import unrelibrary.formatting.JSONRepresentable;

public class UnfurledMediaItem implements JSONRepresentable {
    public final String URL;

    public UnfurledMediaItem(String url) {
        this.URL = url;
    }

    public String toJSON() {
        JSONBuilder toReturnBuilder = new JSONBuilder();
        toReturnBuilder.addStringProperty("url", URL);
        return toReturnBuilder.build();
    }
}
