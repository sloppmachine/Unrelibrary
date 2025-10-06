package unrelibrary.restapi;

import java.util.function.Function;

import unrelibrary.discordobjects.interactions.Interaction;
import unrelibrary.discordobjects.interactions.SlashCommandInteraction;
import unrelibrary.formatting.JSONBuilder;
import unrelibrary.formatting.JSONRepresentable;

// this class is for the implementation of slash commands. this also means local stuff. this class is not (just) a type for data exchange with the api.
public class SlashCommand implements JSONRepresentable {
    public String name; // this name is actually unique, we can use it as an identifier
    public String description; //
    public SlashCommand.Option[] options = null;
    public int[] contexts;
    public Function<SlashCommandInteraction, Interaction.CustomIDUpdatingResponse> action; // returns a response

    // a quicker and simpler constructor
    public SlashCommand(String name, String description, SlashCommand.Option[] options, int[] contexts, Function<SlashCommandInteraction, Interaction.CustomIDUpdatingResponse> action) {
        this.name = name;
        this.description = description;
        this.options = options;
        this.action = action;
        this.contexts = contexts;
    }

    // these options will be transformed into a json when registering the slash command online
    public static class Option implements JSONRepresentable {
        private boolean required = true; // this doesn't support optional options yet
        public String name;
        public String description;
        public int type;

        public boolean autocomplete = false;

        public Option(String name, String description) {
            this.name = name;
            this.description = description;
            this.type = 3;
        }

        public String toJSON() {
            JSONBuilder toReturnBuilder = new JSONBuilder();
            toReturnBuilder.addBooleanProperty("required", required);
            toReturnBuilder.addStringProperty("name", name);
            toReturnBuilder.addStringProperty("description", description);
            toReturnBuilder.addIntProperty("type", type);
            toReturnBuilder.addBooleanProperty("autocomplete", autocomplete);
            return toReturnBuilder.build();
        }
    }

    public String toJSON() {
        JSONBuilder toReturnBuilder = new JSONBuilder();
        toReturnBuilder.addStringProperty("name", name);
        toReturnBuilder.addStringProperty("description", description);
        toReturnBuilder.addIntProperty("type", 1);
        toReturnBuilder.addLiteralProperty("options", JSONBuilder.buildArray(options));
        toReturnBuilder.addIntArrayProperty("contexts", contexts);
        return toReturnBuilder.build();
    }

}
