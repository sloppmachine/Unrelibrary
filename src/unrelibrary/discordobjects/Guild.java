package unrelibrary.discordobjects;

// https://discord.com/developers/docs/resources/guild
public class Guild {
    public final long ID;
    public final String NAME;

    public Guild(long id, String name) {
        this.ID = id;
        this.NAME = name;
    }
}
