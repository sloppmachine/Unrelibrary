package unrelibrary.discordobjects;

// https://discord.com/developers/docs/resources/channel
public class Channel {
    
    public final long ID;
    public final int TYPE;
    public final String NAME;
    public final User[] RECIPIENTS; // for dms

    public Channel(long id, int type, String name, User[] recipients) {
        this.ID = id;
        this.TYPE = type;
        this.NAME = name;
        this.RECIPIENTS = recipients;
    }

}
