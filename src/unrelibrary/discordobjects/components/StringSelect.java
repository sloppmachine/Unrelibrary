package unrelibrary.discordobjects.components;

import unrelibrary.formatting.JSONBuilder;
import unrelibrary.formatting.JSONRepresentable;

public class StringSelect extends Component {
    public final String CUSTOM_ID;
    public final Option[] OPTIONS;
    public final String PLACEHOLDER;

    public StringSelect(String customID, Option[] options, String placeholder) {
        super(3);
        this.CUSTOM_ID = customID;
        this.OPTIONS = options;
        this.PLACEHOLDER = placeholder;
    }

    public static class Option implements JSONRepresentable {
        public final String LABEL;
        public final String VALUE;
        public final String DESCRIPTION;

        public Option(String label, String value, String description) {
            this.LABEL = label;
            this.VALUE = value;
            this.DESCRIPTION = description;
        }

        public String toJSON() {
            JSONBuilder toReturnBuilder = new JSONBuilder();
            toReturnBuilder.addStringProperty("label", LABEL);
            toReturnBuilder.addStringProperty("value", VALUE);
            toReturnBuilder.addStringProperty("description", DESCRIPTION);
            return toReturnBuilder.build();
        }
    }

    public String toJSON() {
        JSONBuilder toReturnBuilder = new JSONBuilder();
        toReturnBuilder.addIntProperty("type", TYPE);
        toReturnBuilder.addStringProperty("custom_id", CUSTOM_ID);
        toReturnBuilder.addLiteralProperty("options", JSONBuilder.buildArray(OPTIONS));
        toReturnBuilder.addStringProperty("placeholder", PLACEHOLDER);
        return toReturnBuilder.build();
    }
}
