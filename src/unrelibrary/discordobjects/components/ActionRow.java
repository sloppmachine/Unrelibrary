package unrelibrary.discordobjects.components;

import unrelibrary.formatting.JSONBuilder;

// https://discord.com/developers/docs/components/reference#action-row
public class ActionRow extends Component {
    public final Component[] COMPONENTS;

    public ActionRow(Component[] components) {
        super(1);
        this.COMPONENTS = components;
    }

    public String toJSON() {
        JSONBuilder toReturnBuilder = new JSONBuilder();
        toReturnBuilder.addIntProperty("type", TYPE);
        toReturnBuilder.addLiteralProperty("components", JSONBuilder.buildArray(COMPONENTS));
        return toReturnBuilder.build();
    }
}
