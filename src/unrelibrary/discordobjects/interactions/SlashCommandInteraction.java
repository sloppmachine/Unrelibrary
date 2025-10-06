package unrelibrary.discordobjects.interactions;

import unrelibrary.discordobjects.GuildMember;
import unrelibrary.discordobjects.User;

// https://discord.com/developers/docs/interactions/receiving-and-responding#interaction-object-application-command-data-structure
public class SlashCommandInteraction extends Interaction {
    public final Data DATA;

    public SlashCommandInteraction(long id, int type, String token, User user, GuildMember member, long channelID, Data data) {
        super(id, type, token, user, member, channelID);
        this.DATA = data;
    }

    // https://discord.com/developers/docs/interactions/receiving-and-responding#interaction-object-resolved-data-structure
    public static class Data {
        public final String NAME;
        public final Option[] OPTIONS;

        public Data(String name, Option[] options) {
            this.NAME = name;
            this.OPTIONS = options;
        }

        // https://discord.com/developers/docs/interactions/receiving-and-responding#interaction-object-application-command-interaction-data-option-structure
        public static class Option {
            public final int TYPE;
            public final String NAME;
            public final String STRING_VALUE; // only supports strings as of now
            
            public Option(int type, String name, String stringValue) {
                this.TYPE = type;
                this.NAME = name;
                this.STRING_VALUE = stringValue;
            }
        }
    }
}
