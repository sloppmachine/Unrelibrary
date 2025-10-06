package unrelibrary.discordobjects.components;

import unrelibrary.formatting.JSONBuilder;

public class Separator extends Component {
    public final boolean DIVIDER;
    public final int SPACING;

    public Separator(boolean divider, int spacing) {
        super(14);
        this.DIVIDER = divider;
        this.SPACING = spacing;
    }

    public String toJSON() {
        JSONBuilder toReturnBuilder = new JSONBuilder();
        toReturnBuilder.addIntProperty("type", TYPE);
        toReturnBuilder.addBooleanProperty("divider", DIVIDER);
        toReturnBuilder.addIntProperty("spacing", SPACING);
        return toReturnBuilder.build();
    }
}
