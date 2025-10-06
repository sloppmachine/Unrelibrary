package unrelibrary.discordobjects.components;

import unrelibrary.formatting.JSONBuilder;

public class Label extends Component {
    public final String LABEL;
    public final String DESCRIPTION;
    public final Component COMPONENT;

    public Label(String label, String description, Component component) {
        super(18);
        this.LABEL = label;
        this.DESCRIPTION = description;
        this.COMPONENT = component;
    }

    public String toJSON() {
        JSONBuilder toReturnBuilder = new JSONBuilder();
        toReturnBuilder.addIntProperty("type", TYPE);
        toReturnBuilder.addStringProperty("label", LABEL);
        toReturnBuilder.addStringProperty("description", DESCRIPTION);
        toReturnBuilder.addLiteralProperty("component", COMPONENT.toJSON());
        return toReturnBuilder.build();
    }
}
