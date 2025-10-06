package unrelibrary.discordobjects;

// https://discord.com/developers/docs/resources/message
public class Message {
    public final long ID;
    public final User USER;
    public final String CONTENT;
    public final long CHANNEL_ID;

    public Message(long id, User user, String content, long channelID) {
        this.ID = id;
        this.USER = user;
        this.CONTENT = content;
        this.CHANNEL_ID = channelID;
    }

    public String visualize() {
        StringBuilder toReturnBuilder = new StringBuilder();
        toReturnBuilder.append(USER.USERNAME + ": " + CONTENT + "\n");
        return toReturnBuilder.toString();
    }
}
