package unrelibrary.discordobjects.interactions;

import unrelibrary.discordobjects.GuildMember;
import unrelibrary.discordobjects.User;
import unrelibrary.discordobjects.components.Component;
import unrelibrary.formatting.JSONBuilder;
import unrelibrary.formatting.JSONRepresentable;
import unrelibrary.restapi.CustomIDListeningUpdate;

// https://discord.com/developers/docs/interactions/receiving-and-responding#interaction-object
public abstract class Interaction {
    public final long ID;
    public final int TYPE;
    public final String TOKEN;
    public final User USER;
    public final GuildMember MEMBER;
    public final long CHANNEL_ID;

    public Interaction(long id, int type, String token, User user, GuildMember guildMember, long channelID) {
        this.ID = id;
        this.TYPE = type;
        this.TOKEN = token;
        this.USER = user;
        this.MEMBER = guildMember;
        this.CHANNEL_ID = channelID;
    }

    public abstract static class Response implements JSONRepresentable {
        
    }

    // internal version of response, but includes a string value for all custom ids that the handler should stop checking for
    public static class CustomIDUpdatingResponse {
        public Response response;
        public CustomIDListeningUpdate customIDListeningUpdate;

        public CustomIDUpdatingResponse(
            Response response,
            CustomIDListeningUpdate customIDListeningUpdate
        ) {
            this.response = response;
            this.customIDListeningUpdate = customIDListeningUpdate;
        }
    }

    // this is a tool for sending message responses
    public static class MessageResponse extends Response {
        public final int TYPE; // 4 is a simple text response message
        
        public Data data = new Data();
        // this is not integrated into the json. if a custom id (key) is used by a future interaction, call the associated function (value)

        public MessageResponse(int type) {
            this.TYPE = type;
        }

        public class Data implements JSONRepresentable {
            public String content;
            public Integer flags;
            public Component[] components;

            public String toJSON() {
                JSONBuilder toReturnBuilder = new JSONBuilder();
                toReturnBuilder.addStringProperty("content", content);
                toReturnBuilder.addIntProperty("flags", flags);
                toReturnBuilder.addLiteralProperty("components", JSONBuilder.buildArray(components));
                return toReturnBuilder.build();
            }
        }

        public String toJSON() {
            JSONBuilder toReturnBuilder = new JSONBuilder();
            toReturnBuilder.addIntProperty("type", TYPE);
            toReturnBuilder.addLiteralProperty("data", data.toJSON());
            return toReturnBuilder.build();
        }
    }

    public static class ModalResponse extends Response {
        public final int TYPE = 9; // 9 is the only modal type.
        public Data data = new Data();

        public class Data implements JSONRepresentable {
            public String customID;
            public String title;
            public Component[] components;

            public String toJSON() {
                JSONBuilder toReturnBuilder = new JSONBuilder();
                toReturnBuilder.addStringProperty("custom_id", customID);
                toReturnBuilder.addStringProperty("title", title);
                toReturnBuilder.addLiteralProperty("components", JSONBuilder.buildArray(components));
                return toReturnBuilder.build();
            }
        }

        public String toJSON() {
            JSONBuilder toReturnBuilder = new JSONBuilder();
            toReturnBuilder.addIntProperty("type", TYPE);
            toReturnBuilder.addLiteralProperty("data", data.toJSON());
            return toReturnBuilder.build();
        }
    }
}