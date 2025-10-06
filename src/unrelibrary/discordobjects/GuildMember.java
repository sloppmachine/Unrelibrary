package unrelibrary.discordobjects;

// this is a user with additional data related to a server, like their nickname, their join date etc.
// https://discord.com/developers/docs/resources/guild
public class GuildMember {
    public final long GUILD_ID; // this isn't part of the object that discord sends, but knowing which guild a member belongs to is good to know
    public final User USER;
    public final String NICK;
    public final String AVATAR;

    public GuildMember(long guildID, User user, String nick, String avatar) {
        this.GUILD_ID = guildID;
        this.USER = user;
        this.NICK = nick;
        this.AVATAR = avatar;
    }

    // this is the avatar people in the server see, i.e. the server nitro pfp is there is one, otherwise the global onee.
    public String getVisibleAvatarURL() {
        if (AVATAR != null) {
            if (AVATAR.substring(0, 2).equals("a_")) {
                return "https://cdn.discordapp.com/guilds/" + GUILD_ID + "/users/" + USER.ID + "/avatars/" + AVATAR + ".webp?animated=true";
            } else {
                return "https://cdn.discordapp.com/guilds/" + GUILD_ID + "/users/" + USER.ID + "/avatars/" + AVATAR + ".webp";
            }
        } else {
            return USER.getAvatarURL();
        }
    }
}
