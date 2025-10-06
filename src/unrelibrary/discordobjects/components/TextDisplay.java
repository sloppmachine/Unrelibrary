package unrelibrary.discordobjects.components;

import unrelibrary.formatting.JSONBuilder;

// https://discord.com/developers/docs/components/reference#text-display
public class TextDisplay extends Component {
    public final String CONTENT;

    public TextDisplay(String content) {
        super(10);
        this.CONTENT = content;
    }

    public String toJSON() {
        JSONBuilder toReturnBuilder = new JSONBuilder();
        toReturnBuilder.addIntProperty("type", TYPE);
        toReturnBuilder.addStringProperty("content", CONTENT);
        return toReturnBuilder.build();
    }
}
