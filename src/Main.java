import unrelibrary.DiscordBot;
import unrelibrary.restapi.SlashCommand;
import unrelibrary.discordobjects.interactions.Interaction;
import unrelibrary.discordobjects.interactions.SlashCommandInteraction;
import unrelibrary.discordobjects.interactions.Interaction.MessageResponse;
import unrelibrary.gatewayapi.EventReceiver;

// this is just for demonstration.
public class Main {

    public static Interaction.CustomIDUpdatingResponse echo(SlashCommandInteraction slashCommandInteraction) {
        MessageResponse messageResponse = new MessageResponse(4); // this is the message you want to send to discord. Use type 4 for text messages: https://discord.com/developers/docs/interactions/receiving-and-responding#interaction-response-object-interaction-callback-type
        messageResponse.data.content = slashCommandInteraction.DATA.OPTIONS[0].STRING_VALUE; // send the same text that the user gave in the first (and only) field
        Interaction.CustomIDUpdatingResponse customIDUpdatingResponse = new Interaction.CustomIDUpdatingResponse(
            messageResponse,
            null // here, you can add a CustomIDListeningUpdate if you want to listen to custom ids.
        ); // respond with a message
        return customIDUpdatingResponse;
    }

    public static void main(String[] args) {
        DiscordBot discordBot = new DiscordBot(true, 80, true, "./data/confidential.json", true);
        discordBot.initialize(new EventReceiver());

        // these names need to be all lowercase.
        SlashCommand echo = new SlashCommand(
            "echo",
            "Echoes back over the internet",
            new SlashCommand.Option[] {
                new SlashCommand.Option(
                    "noise",
                    "Which noise do you want to hear echoing back?"
                )
            },
            new int[] {0, 1}, // 0 is for servers, 1 for bot DMs.
            Main::echo
        );

        discordBot.registerSlashCommand("echo", echo);

        discordBot.goOnline();
        discordBot.control();
        
        discordBot.goOffline();
    }
}