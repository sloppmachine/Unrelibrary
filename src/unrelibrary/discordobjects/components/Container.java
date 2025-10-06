package unrelibrary.discordobjects.components;

import unrelibrary.formatting.JSONBuilder;

// https://discord.com/developers/docs/components/reference#container
public class Container extends Component {
    public final Component[] COMPONENTS;
    public final int ACCENT_COLOR;

    public Container(Component[] components, int accentColor) {
        super(17);
        this.COMPONENTS = components;
        this.ACCENT_COLOR = accentColor;
    }

    public String toJSON() {
        JSONBuilder toReturnBuilder = new JSONBuilder();
        toReturnBuilder.addIntProperty("type", TYPE);
        toReturnBuilder.addLiteralProperty("components", JSONBuilder.buildArray(COMPONENTS));
        toReturnBuilder.addIntProperty("accent_color", ACCENT_COLOR);
        return toReturnBuilder.build();
    }
}
