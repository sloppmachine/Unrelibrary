package unrelibrary.discordobjects.interactions;

import unrelibrary.discordobjects.GuildMember;
import unrelibrary.discordobjects.Message;
import unrelibrary.discordobjects.User;

// discord sends this type of interaction everytime someone has interacted with a component
// https://discord.com/developers/docs/interactions/receiving-and-responding#interaction-object-message-component-data-structure
public class ComponentInteraction extends Interaction {
    public final Message MESSAGE;
    public final Data DATA;

    public ComponentInteraction(long id, int type, String token, User user, GuildMember member, long channelID, Message message, Data data) {
        super(id, type, token, user, member, channelID);
        this.MESSAGE = message;
        this.DATA = data;
    }

    // https://discord.com/developers/docs/components/reference#string-select-select-option-structure
    public static class Data {
        public final String CUSTOM_ID;
        public final int COMPONENT_TYPE;
        public final String[] VALUES; // for string select
        public final String VALUE; // for text input

        public Data(String customID, int componentType, String[] values, String value) {
            this.CUSTOM_ID = customID;
            this.COMPONENT_TYPE = componentType;
            this.VALUES = values;
            this.VALUE = value;
        }
    }
}