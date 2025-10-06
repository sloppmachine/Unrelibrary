package unrelibrary.discordobjects;

// https://discord.com/developers/docs/resources/user
public class User {
    public final long ID;
    public final String USERNAME;
    public final String GLOBAL_NAME;
    public final String AVATAR;

    public User(long id, String username, String globalName, String avatar) {
        this.ID = id;
        this.USERNAME = username;
        this.GLOBAL_NAME = globalName;
        this.AVATAR = avatar;
    }

    public String getAvatarURL() {
        if (AVATAR != null) {
            if (AVATAR.substring(0, 2).equals("a_")) {
                return "https://cdn.discordapp.com/avatars/" + ID + "/" + AVATAR + ".webp?animated=true";
            } else {
                return "https://cdn.discordapp.com/avatars/" + ID + "/" + AVATAR + ".webp";
            }
        } else {
            return null;
        }
    }
}