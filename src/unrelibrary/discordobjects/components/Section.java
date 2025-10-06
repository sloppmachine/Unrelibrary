package unrelibrary.discordobjects.components;

import unrelibrary.formatting.JSONBuilder;

//https://discord.com/developers/docs/components/reference#text-input
public class Section extends Component {
    public final Component[] COMPONENTS;
    public final Component ACCESSORY;

    public Section(Component[] components, Component accessory) {
        super(9);
        this.COMPONENTS = components;
        this.ACCESSORY = accessory;
    }

    public String toJSON() {
        JSONBuilder toReturnBuilder = new JSONBuilder();
        toReturnBuilder.addIntProperty("type", TYPE);
        toReturnBuilder.addLiteralProperty("components", JSONBuilder.buildArray(COMPONENTS));
        toReturnBuilder.addLiteralProperty("accessory", ACCESSORY.toJSON());
        return toReturnBuilder.build();
    }
}