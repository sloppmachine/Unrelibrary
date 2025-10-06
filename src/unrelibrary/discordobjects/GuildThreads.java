package unrelibrary.discordobjects;

// since requesting guild channels and guild threads are two separate actions, this class is there to explicitly catch only threads.
// it doesn't have any further meaning in the discord api.
public class GuildThreads {
    public final Channel[] THREADS;

    public GuildThreads(Channel[] threads) {
        this.THREADS = threads;
    }
}
