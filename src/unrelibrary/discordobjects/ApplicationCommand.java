package unrelibrary.discordobjects;

import unrelibrary.formatting.JSONBuilder;
import unrelibrary.formatting.JSONRepresentable;

// this is the type of command that is "integrated" into discord, like a slash command - not the old prefix command stuff.
// https://discord.com/developers/docs/interactions/application-commands#application-command-object
public class ApplicationCommand implements JSONRepresentable {
    public final long ID;
    public final int TYPE;
    public final String NAME;
    public final String DESCRIPTION;
    public final ApplicationCommand.Option[] OPTIONS;
    public final int[] CONTEXTS;

    public ApplicationCommand(long id, int type, String name, String description, ApplicationCommand.Option[] options, int[] contexts) {
        this.ID = id;
        this.TYPE = type;
        this.NAME = name;
        this.DESCRIPTION = description;
        this.OPTIONS = options;
        this.CONTEXTS = contexts;
    }
    
    // https://discord.com/developers/docs/interactions/application-commands#application-command-object-application-command-option-structure
    public static class Option implements JSONRepresentable {
        public final int TYPE;
        public final String NAME;
        public final String DESCRIPTION;

        public Option(int type, String name, String description) {
            this.TYPE = type;
            this.NAME = name;
            this.DESCRIPTION = description;
        }

        public String toJSON() {
            JSONBuilder toReturnBuilder = new JSONBuilder();
            toReturnBuilder.addIntProperty("type", TYPE);
            toReturnBuilder.addStringProperty("name", NAME);
            toReturnBuilder.addStringProperty("description", DESCRIPTION);
            return toReturnBuilder.build();
        }
    }

    public String toJSON() {
        JSONBuilder toReturnBuilder = new JSONBuilder();
        toReturnBuilder.addLongProperty("id", ID);
        toReturnBuilder.addIntProperty("type", TYPE);
        toReturnBuilder.addStringProperty("name", NAME);
        toReturnBuilder.addStringProperty("description", DESCRIPTION);
        toReturnBuilder.addLiteralProperty("options", JSONBuilder.buildArray(OPTIONS));
        return toReturnBuilder.build();
    }
}
