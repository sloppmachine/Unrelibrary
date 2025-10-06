package unrelibrary.discordobjects.components;

import unrelibrary.formatting.JSONBuilder;

// https://discord.com/developers/docs/components/reference#action-row
public class Button extends Component {
    public final String CUSTOM_ID;
    public final int STYLE;
    public final String LABEL;

    public Button(String customID, int style, String label) {
        super(2);
        this.CUSTOM_ID = customID;
        this.STYLE = style;
        this.LABEL = label;
    }

    public String toJSON() {
        JSONBuilder toReturnBuilder = new JSONBuilder();
        toReturnBuilder.addIntProperty("type", TYPE);
        toReturnBuilder.addStringProperty("custom_id", CUSTOM_ID);
        toReturnBuilder.addIntProperty("style", STYLE);
        toReturnBuilder.addStringProperty("label", LABEL);
        return toReturnBuilder.build();
    }
}
