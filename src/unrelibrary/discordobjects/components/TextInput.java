package unrelibrary.discordobjects.components;

import unrelibrary.formatting.JSONBuilder;

public class TextInput extends Component {
    public final String CUSTOM_ID;
    public final int STYLE;
    public final String PLACEHOLDER;
    
    public TextInput(String customID, int style, String placeholder) {
        super(4);
        this.CUSTOM_ID = customID;
        this.STYLE = style;
        this.PLACEHOLDER = placeholder;
    }

    public String toJSON() {
        JSONBuilder toReturnBuilder = new JSONBuilder();
        toReturnBuilder.addIntProperty("type", TYPE);
        toReturnBuilder.addStringProperty("custom_id", CUSTOM_ID);
        toReturnBuilder.addIntProperty("style", STYLE);
        toReturnBuilder.addStringProperty("placeholder", PLACEHOLDER);
        return toReturnBuilder.build();
    }
}
