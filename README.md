# Unrelibrary

## About

This is a small set of tools made in Java for quickly setting up and running a discord bot. I built this as a personal holiday project, so it's rather for small fun than for a more dedicated bot. For those, there are many other libraries that you would use rather than this one, and they are probably better documented than this one.

## What you can use Unrelibrary for
- You can locally define slash commands (a kind of what discord calls an application command) with names, descriptions, options and behaviour. Unrelibrary will automatically sync your commands with discord, and respond according to your specification
- You can control some visuals of a Discord Message using Componenents
- You can exercise manual control from the terminal and chat from your bot's perspective, receiving messages, reading them and sending them
- You can edit and remove messages with code, providing your bot has the necessary permissions

## What Unrelibrary can't do
- Start and run an activity on discord
- Use the entire slash command functionality, like autocompleting options or forcing different input types than strings on the user end
- Provide structure necessary to work on a larger amount of servers, like sharding
- Read group DMs
- Run multiple bots at once in the same project (untested)

## How to use

First of all, you need to create a Discord bot: https://discord.com/developers/applications

Once you have created your bot, you can get its token, public key, and application ID on the Discord Developer portal, and choose a port for your application to use (8000 is a good choice). Put it all in a json file with the necessary information like so:

```
{
    "port": 8000,
    "token": "yourtokenhere",
    "publicKey": "yourpublickeyhere",
    "applicationID": 2025
}

```

Download the source files and paste them into your project. Now, in your main file (or elsewhere), you can use the class `unrelibrary.DiscordBot` as your starting point.

```java
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
            new int[] {0, 1},
            Main::echo
        );

        discordBot.registerSlashCommand("echo", echo);

        discordBot.goOnline();
        discordBot.control();
        
        discordBot.goOffline();
    }
}
```

The `DiscordBot` constructor takes 5 parameters: whether you only want to use the [REST API](#rest-and-gateway), how big the separators in the terminal should turn out when you manually control the bot, whether the bot should wait for its [endpoint to be verified](#endpoint-verification), where the confidential data is, and whether it should be verbose (show you what it's doing). To configure a bot, you have to call `initialize()` first and optionally specify an [Event Listener](#rest-and-gateway). A slash command needs a name, a description, an array of options, its contexts, and a function to execute. Slash command functions take a SlashCommandInteraction and return a CustomIDUpdatingFuction.

### Dependencies
Unrelibrary requires **Bouncycastle** for some security functionality.

### Discord Developer Portal
On the Discord Developer Portal, you can make your own Discord Bot (https://discord.com/developers/applications) and see the official API documentation (https://discord.com/developers/docs/intro)

### Important Mentions
- This is a hobby project of mine, and it should also be said that i have do not have a big prior experience with bot implementation. It's a learning and building practice for myself (even this readme), and it's cool that it exists, but it's still something personal and experimental. There are [many more sophisticated libraries for Discord bots in many languages](https://discord.com/developers/docs/developer-tools/community-resources#libraries).
- Unrelibrary does not automatically check whether the Discord bot has necessary permissions before it tries to act. You need to make that sure yourself.
- Don't change the version of the API you use. I haven't tested that.
- Discord forces slash commands to have unique names, and they need to be in lowercase [along other things](https://discord.com/developers/docs/interactions/application-commands#application-command-object).
- If you want to use components: You can only use a total of 40 component in one message, you need to use the component flag if you do want to use component, unrelibrary doesn't have every single component, and furthermore, some component can only be placed in specific others.
- You can attach custom IDs to components, and specify which ones to listen for and what to do in a CustomIDListeningUpdate you specifiy with your response.


### Notes on Implementation
- Discord transmits its 64-bit integers ("snowflakes") as strings, so as to not cause overflow on 32-bit systems.
- Stuff is generally marked as final when it isn't meant to be changed locally. Some things are handled on discord's side, and instead overwritten locally after sending a request to discord.


### REST and Gateway
The Discord API is broadly divided into two parts, the REST API and the Gateway API. Gateway is used for receiving live events (like messages and status updates), and REST is used for anything else. Slash commands run entirely on REST, so using Gateway is optional in unrelibrary. That being said, using Gateway requires a constant exchange with discord, and unrelibrary isn't battle-tested enough for me to be confident in saying it works completely. As of now, the only event you can listen to actively is when a message is sent.

### Endpoint verification
To receive slash commands, you need to have an interaction endpoint set up (that is, a URL for discord to send the interactions to). Until now, i always used ngrok to connect the port I use to a generated URL. **You need to append /bot/interactions to your URL**. You need to then specify the URL [here](https://discord.com/developers/docs/interactions/application-commands#application-command-object).

## Examples
I haven't written a complete documentation for everything (yet) (even though it might have been a good practice), but a running example would be [Filterkaffee](https://github.com/sloppmachine/Filterkaffee).

## License
This piece of code uses the MIT license.