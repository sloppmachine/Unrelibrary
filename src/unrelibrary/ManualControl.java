package unrelibrary;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import java.util.Scanner;

import unrelibrary.discordobjects.Guild;
import unrelibrary.discordobjects.GuildMember;
import unrelibrary.discordobjects.Message;
import unrelibrary.discordobjects.User;
import unrelibrary.formatting.GeneralFormatter;
import unrelibrary.restapi.ServerResponseException;
import unrelibrary.discordobjects.Channel;

public class ManualControl {
    private final String SEPERATOR;
    private boolean quit;
    private APICommunicationManager apiCommunicationManager;
    private Scanner scanner;
    private Guild currentGuild;
    private Channel currentChannel;
    private Map<Long, Channel> dms;
    private List<News> news;

    private static final String DEFAULT_GET_PARAMETER = "10";

    public ManualControl(int seperatorWidth, APICommunicationManager apiCommunicationManager) throws ServerResponseException {
        StringBuilder seperatorBuilder = new StringBuilder();
        for (int i = 0; i < seperatorWidth; i++) {
            seperatorBuilder.append("-");
        }
        this.apiCommunicationManager = apiCommunicationManager;
        this.SEPERATOR = seperatorBuilder.toString();
        this.scanner = new Scanner(System.in);
        this.dms = new TreeMap<Long, Channel>();
        this.news = new LinkedList<News>();
        this.news.add(new News(News.Type.WELCOME, "Welcome to the Unrelibrary manual control! Type \"help\" for an overview of commands, and \"important\" for some important information.", null)); 
    }

    public class News {
        public static enum Type {WELCOME, GATEWAY_CONNECTED, DM_DISCOVERED, NEW_PUBLIC_MESSAGE, NEW_DM};
        public final Type TYPE;
        public final String DESC;
        public final Long CHANNEL_ID; // nullable

        public News(Type type, String desc, Long channelID) {
            this.TYPE = type;
            this.DESC = desc;
            this.CHANNEL_ID = channelID;
        }
    }

    public void gatewayConnected() {
        news.add(new News(News.Type.GATEWAY_CONNECTED, "Connection to the Gateway API established! You will now be notified of new messages.", null));
        return;
    }

    private void addNumberedPrefix(StringBuilder stringBuilder, int number) {
        String numberedPrefix = "    (" + (number) + "):";
        int whitespaceToAdd = 11 - numberedPrefix.length();
        stringBuilder.append(numberedPrefix);
        for (int j = 0; j < whitespaceToAdd; j++) {
            stringBuilder.append(" ");
        }
        return;
    }

    private String[] unescapeDiscordStrings(String string) {
        String[] toReturn = string.split("\\\\n");
        for (int i = 0; i < toReturn.length; i++) {
            toReturn[i] = toReturn[i].replace("\\\"", "\"");
        }
        return toReturn;
    }

    private void printNewLine() {
        System.out.println(SEPERATOR);
        printNews();
        printCurrentLocation();
        System.out.print(">   ");
        return;
    }

    private void printNews() {
        if (news.isEmpty()) {
            System.out.println("You have 0 news.");
        } else {
            System.out.println("There are " + news.size() + " news! Type \"news\" to see them.");
        }
        return;
    }

    private void printCurrentLocation() {
        System.out.print("Currently looking at:    ");
        if (currentGuild == null && currentChannel == null) {
            System.out.println("(no channel selected)");
        } else if (currentGuild == null && currentChannel != null) {
            User recipient = currentChannel.RECIPIENTS[0];
            System.out.println("DMs -> " + recipient.GLOBAL_NAME + " (" + recipient.USERNAME + ")");
        } else if (currentGuild != null && currentChannel == null) {
            System.out.println(currentGuild.NAME + " -> (You need to select a channel. Try \"switchchannel\")");
        } else {
            System.out.println(currentGuild.NAME + " -> " + currentChannel.NAME);
        } 
        return;
    }

    private String removeSpacesAtFront(String string) {
        int firstChar = 0;
        while (true) {
            if (firstChar >= string.length()) {
                return "";
            } else {
                if (string.charAt(firstChar) == ' ') {
                    firstChar++; 
                } else {
                    return string.substring(firstChar, string.length());
                }
            }
        }
    }

    private void unknownCommand() {
        System.out.println("Unknown command. Type \"help\" to be helped.");
        return;
    }

    private void helpCommand() {
        System.out.println("You are currently using the manual control system provided with Unrelibrary, which allows you to read and write messages on " + 
            "Discord from your bot's account, explore channels and, provided you are connected to the Gateway API, receive news about new Messages and DMs.\n" + 
            "You can type the following commands into the terminal: \n\n" +
            "\"help\"    - You just found out what this does yourself. Well done!\n" + 
            "\"important\"    - Displays noteworthy information. Please, take a look.\n" + 
            "\"news\"    - Displays your news.\n" + 
            "\"switch\"    - Lets you switch the current server or channel your bot is looking at. You will be prompted for further input.\n" + 
            "\"switchchannel\"    - Like \"switch\", but will stay in the current server or the DMs and only ask you about channels.\n" + 
            "\"dm\" <user id>    - Opens a DM channel to a specific user if it doesn't exist already. Automatically switches your bot to look at the DM in question.\n" + 
            "\"get\" <number>    - Loads and displays the last <number> of messages from the channel your bot is currently looking at. With the Discord API, this takes more " + 
            "time than you are used to as a normal user. If <number> isn't specified, it defaults to " + DEFAULT_GET_PARAMETER + ".\n" + 
            "\"send\" <message>    - Sends a message from the bot's user to the channel it is currently looking at.\n" + 
            "\"reconnect\"    - This closes the connection to the Gateway API (if there is one) and reconnects. Note that it can take up to a minute or so for this to take effect.\n" + 
            "\"quit\"    - Quits the manual control system.");
        return;
    }

    private void importantCommand() {
        System.out.println("DMs on the Discord API are a bit weird. You can't list them all, so you can't start with a list of all users who you once DM'ed with. " + 
        "Instead you need to (re)open, or \"discover\" the DMs, either by getting the DM channel to a user by their ID, or by receiving a message from them" +
        "(assuming you have an active connection to the Gateway API.)");
        System.out.println("Don't try to open new DMs too quickly or too often, it might lead to Discord rate limiting you or blocking you " +
            "from opening new ones. This doesn't apply to people that you have already exchanged DMs with at least once.");
        System.out.println("You can use this manual control system only using the REST API and without ever touching the Gateway API. However, you will " + 
            "need an active connection to the Gateway API to receive news of new messages, including DMs. Without the Gateway API, you will just need to look " + 
            "for new messages yourself.");
        return;
    }

    private void newsCommand() {
        checkGateway();
        if (news.isEmpty()) {
            System.out.println("There are no news.");
            return;
        } else {
            StringBuilder newsDisplay = new StringBuilder("There are " + news.size() + " news: \n");
            for (News newsEntry : news) {
                newsDisplay.append(">>> " + newsEntry.DESC + "\n");
            }
            System.out.println(newsDisplay.toString());
            // some news types are removed directly after viewing
            int i = 0;
            while (true) {
                if (i >= news.size()) {
                    break;
                }
                News.Type newsType = news.get(i).TYPE;
                if (newsType == News.Type.WELCOME || newsType == News.Type.DM_DISCOVERED || newsType == News.Type.GATEWAY_CONNECTED) {
                    news.remove(i);
                    i--;
                }
                i++;
            }
            return;
        }
    }

    private void checkGateway() {
        checkForNewMessages();
        // add more here later if you feel like it
        return;
    }

    private void checkForNewMessages() {
        long channelID;
        boolean known = false; // if there is already news of this channel having new messages, we don't have to do anything at all
        while (!apiCommunicationManager.gatewayManager.newMessageChannels.isEmpty()) {
            channelID = apiCommunicationManager.gatewayManager.newMessageChannels.removeFirst();
            for (News newsEntry : news) {
                if (newsEntry.TYPE == News.Type.NEW_PUBLIC_MESSAGE || newsEntry.TYPE == News.Type.NEW_DM) {
                    if (newsEntry.CHANNEL_ID == channelID) {
                        known = true;
                    }
                }
            }
            if (!known) {
                // we need to check which type of channel it comes from
                Channel channel;
                try {
                    channel = apiCommunicationManager.restManager.getChannel(channelID);
                } catch (ServerResponseException serverResponseException) {
                    GeneralFormatter.printException("Couldnt get channel details when checking for new messages.", serverResponseException);
                    return;
                }
                if (channel.TYPE == 1) { // its a dm
                    if (!dms.keySet().contains(channel.RECIPIENTS[0].ID)) {
                        dms.put(channel.RECIPIENTS[0].ID, channel);
                        news.add(new News(News.Type.DM_DISCOVERED, "Discovered a new DM with " + channel.RECIPIENTS[0].USERNAME + "!", channelID));
                    }
                    news.add(new News(News.Type.NEW_DM, "Got a DM from user " + channel.RECIPIENTS[0].USERNAME + ".", channelID));
                } else if (channel.TYPE == 3) {
                    // group dm, currently isn't supported.
                } else {
                    news.add(new News(News.Type.NEW_PUBLIC_MESSAGE, "New messages in " + channel.NAME, channelID));
                }
            }
        }
        return;
    }

    private void reconnectCommand() {
        if (apiCommunicationManager.gatewayManager.isConnected() == true) {
            apiCommunicationManager.gatewayManager.reconnect();
        } else {
            System.out.println("Can't reconnect because there is no gateway connection as of now.");
        }

        return;
    }

    private void quitCommand() {
        quit = true;
        return;
    }

    private void switchChannel(boolean skipGuildSelect) {
        if (!skipGuildSelect) {
            Guild[] guilds;
            System.out.println("Loading servers...");
            try {
                guilds = apiCommunicationManager.restManager.getJoinedGuilds();
            } catch (ServerResponseException serverResponseException) {
                GeneralFormatter.printException("Couldn't get list of servers.", serverResponseException);
                return;
            }
            System.out.println("Select a server:");
            StringBuilder guildSelect = new StringBuilder("    (0):   cancel \n    (1):   DMs \n");
            Guild guild;
            for (int i = 0; i < guilds.length; i++) {
                guild = guilds[i];
                addNumberedPrefix(guildSelect, i + 2);
                guildSelect.append(guild.NAME + "\n");
            }
            System.out.println(guildSelect.toString());
            System.out.println("Input a number: ");
            try {
                int number = Integer.valueOf(scanner.nextLine());
                if (number == 0) {
                    System.out.println("Cancelled\n");
                    return;
                } else if (number == 1) {
                    currentGuild = null;
                } else if (number >= 2 && number < guilds.length + 2) {
                    currentGuild = guilds[number - 2];
                } else {
                    System.out.println("Invalid response, cancelling\n");
                    return;
                }
            } catch (NumberFormatException numberFormatException) {
                System.out.println("Invalid response, cancelling\n");
                return;
            }
        }
        
        System.out.println("Loading channels...");
        currentChannel = null;
        Channel[] channels; // used in multiple mutually exclusive blocks
        Channel channel; // so we dont have to redeclare it all the time
        if (currentGuild == null) {
            if (dms.isEmpty()) {
                System.out.println("No dms have been discovered. You need to discover them first, as mentioned in \"important\". Use the command \"dm\" or get a message for them to be discovered.");
            } else {
                Map<Integer, Long> numberToUserID = new TreeMap<Integer, Long>();
                StringBuilder dmSelect = new StringBuilder("    (0):   cancel \n");
                int currentIndex = 1;
                for (Map.Entry<Long, Channel> dmEntry : dms.entrySet()) {
                    System.out.println(currentIndex);
                    addNumberedPrefix(dmSelect, currentIndex);
                    User recipient = dmEntry.getValue().RECIPIENTS[0];
                    dmSelect.append(recipient.GLOBAL_NAME + " (" + recipient.USERNAME + ")\n");
                    numberToUserID.put(currentIndex, dmEntry.getKey());
                    currentIndex++;
                }
                System.out.println(dmSelect.toString());
                System.out.println("Input a number: ");
                try {
                    int number = Integer.valueOf(scanner.nextLine());
                    if (number == 0) {
                        System.out.println("Cancelled\n");
                        return;
                    } else if (number >= 1 && number < numberToUserID.size() + 1) {
                        currentChannel = dms.get(numberToUserID.get(number));
                        return;
                    } else {
                        System.out.println("Invalid response, cancelling\n");
                        return;
                    }
                } catch (NumberFormatException numberFormatException) {
                    System.out.println("Invalid response, cancelling\n");
                    return;
                }
            }
        } else {
            try {
                Channel[] nonThreadChannels = apiCommunicationManager.restManager.getGuildChannels(currentGuild.ID);
                Channel[] threadChannels = apiCommunicationManager.restManager.getGuildThreads(currentGuild.ID).THREADS;
                channels = new Channel[nonThreadChannels.length + threadChannels.length];
                System.arraycopy(nonThreadChannels, 0, channels, 0, nonThreadChannels.length);
                System.arraycopy(threadChannels, 0, channels, nonThreadChannels.length, threadChannels.length);
            } catch (ServerResponseException serverResponseException) {
                GeneralFormatter.printException("Couldn't get list of channels.", serverResponseException);
                return;
            }
            System.out.println("Select a channel:");
            StringBuilder channelSelect = new StringBuilder("    (0):   cancel \n");
            for (int i = 0; i < channels.length; i++) {
                channel = channels[i];
                addNumberedPrefix(channelSelect, i + 1);
                channelSelect.append(channel.NAME + "\n");
            }
            System.out.println(channelSelect.toString());
            System.out.println("Input a number: ");
            try {
                int number = Integer.valueOf(scanner.nextLine());
                if (number == 0) {
                    System.out.println("Cancelled");
                    return;
                } else if (number >= 1 && number < channels.length + 1) {
                    currentChannel = channels[number - 1];
                    return;
                } else {
                    System.out.println("Invalid response, cancelling");
                    return;
                }
            } catch (NumberFormatException numberFormatException) {
                System.out.println("Invalid response, cancelling");
                return;
            }
        }
    }

    private void markChannelAsRead(long channelID) {
        int i = 0;
        News currentNews;
        while (true) {
            if (i >= news.size()) {
                break;
            }
            currentNews = news.get(i);
            if (currentNews.CHANNEL_ID != null) {
                if (currentNews.CHANNEL_ID == channelID) {
                    news.remove(i);
                    i--;
                }
            }
            i++;
        }
        return;
    }

    private void switchCommand() {
        switchChannel(false);
        return;
    }

    private void switchChannelCommand() {
        switchChannel(true);
        return;
    }

    private void getCommand() {
        getWithArgumentCommand(DEFAULT_GET_PARAMETER);
        return;
    }

    private void getWithArgumentCommand(String argument) {
        int amount = 0;
        try {
            amount = Integer.valueOf(removeSpacesAtFront(argument));
        } catch (NumberFormatException numberFormatException) {
            System.out.println("Invalid syntax. Correct form:    get <number>");
            return;
        }
        if (currentChannel == null) {
            System.out.println("You need to select a channel first, with \"switch\" or \"switchchannel\"");
        } else {
            Message[] messages;
            try {
                messages = apiCommunicationManager.restManager.getLastMessages(currentChannel.ID, amount);
            } catch (ServerResponseException serverResponseException) {
                GeneralFormatter.printException("Couldn't get messages.", serverResponseException);
                return;
            }
            StringBuilder messageDisplay = new StringBuilder();
            Message currentMessage;
            User sender;
            long lastUserID = 0;
            if (currentGuild != null) {
                Map<Long, String> nicknames = new TreeMap<Long, String>();
                // iterate backwards
                for (int i = messages.length - 1; i >= 0; i--) {
                    currentMessage = messages[i];
                    sender = currentMessage.USER;
                    if (!nicknames.containsKey(sender.ID)) {
                        GuildMember guildMember;
                        try {
                            guildMember = apiCommunicationManager.restManager.getGuildMember(currentGuild.ID, sender.ID);
                        } catch (ServerResponseException serverResponseException) {
                            GeneralFormatter.printException("Couldnt get info about server member.", serverResponseException);
                            return;
                        }
                        nicknames.put(sender.ID, guildMember.NICK);
                    }
                    if (sender.ID != lastUserID) {
                        messageDisplay.append("\n");
                        if (nicknames.get(sender.ID) != null) {
                            messageDisplay.append(nicknames.get(sender.ID) + " ");
                        } else {
                            messageDisplay.append("(no nickname) ");
                        }
                        if (sender.GLOBAL_NAME != null) {
                            messageDisplay.append("(global name: " + sender.GLOBAL_NAME + ") ");
                        } else {
                            messageDisplay.append("(no global name) ");
                        }
                        messageDisplay.append("(username: " + sender.USERNAME + ") (user id: " + sender.ID + ") \n");
                    }
                    String[] lines = unescapeDiscordStrings(currentMessage.CONTENT);
                    for (int j = 0; j < lines.length; j++) {
                        if (j == 0) {
                            messageDisplay.append(">>> ");
                        } else {
                            messageDisplay.append("    ");
                        }
                        messageDisplay.append(lines[j] + "\n");
                    }
                    lastUserID = sender.ID;
                }
            } else {
                for (int i = messages.length - 1; i >= 0; i--) {
                    currentMessage = messages[i];
                    sender = currentMessage.USER;
                    if (sender.ID != lastUserID) {
                        messageDisplay.append("\n");
                        if (sender.GLOBAL_NAME != null) {
                            messageDisplay.append("(global name: " + sender.GLOBAL_NAME + ") ");
                        } else {
                            messageDisplay.append("(no global name) ");
                        }
                        messageDisplay.append("(user name: " + sender.USERNAME + ") \n");
                    }
                    String[] lines = unescapeDiscordStrings(currentMessage.CONTENT);
                    for (int j = 0; j < lines.length; j++) {
                        if (j == 0) {
                            messageDisplay.append(">>> ");
                        } else {
                            messageDisplay.append("    ");
                        }
                        messageDisplay.append(lines[j] + "\n");
                    }
                    lastUserID = sender.ID;
                }
            }
            System.out.println(messageDisplay.toString());
            markChannelAsRead(currentChannel.ID);
            return;
        }
    }

    private void dmCommand(String argument) {
        long userID = 0;
        try {
            userID = Long.valueOf(removeSpacesAtFront(argument));
        } catch (NumberFormatException numberFormatException) {
            System.out.println("Invalid syntax. Correct form:    dm <user-id>");
            return;
        }
        if (dms.containsKey(userID)) {
            currentGuild = null;
            currentChannel = dms.get(userID);
            return;
        } else {
            try {
                Channel dm = apiCommunicationManager.restManager.getDMChannel(userID);
                dms.put(userID, dm);
                currentGuild = null;
                currentChannel = dm;
                news.add(new News(News.Type.DM_DISCOVERED, "Discovered a new DM with " + dm.RECIPIENTS[0].USERNAME + "!", dm.ID));
                return;
            } catch (ServerResponseException serverResponseException) {
                GeneralFormatter.printException("Couldnt open DM channel.", serverResponseException);
                return;
            }
        }
    }

    private void sendWithArgumentCommand(String argument) {
        if (currentChannel == null) {
            System.out.println("Select a channel first!");
            return;
        } else {
            try {
                apiCommunicationManager.restManager.sendMessage(currentChannel.ID, argument);
                return;
            } catch (ServerResponseException serverResponseException) {
                GeneralFormatter.printException("Couldnt send message.", serverResponseException);
                return;
            }
        }
    }

    // control the bot from the terminal
    // warning: this occupies the thread it was called on
    public void control() {
        String input;
        quit = false;

        while (!quit) {
            printNewLine();
            while (!scanner.hasNextLine()) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException interruptedException) {
                    // who even dares interrupt the main thread????
                }
            }
            input = removeSpacesAtFront(scanner.nextLine());
            System.out.println();
            if (input.indexOf(" ") == -1) {
                // command without arguments
                if (input.equals("help")) {
                    helpCommand();
                } else if (input.equals("important")) {
                    importantCommand();
                } else if (input.equals("news")) {
                    newsCommand();
                } else if (input.equals("switch")) {
                    switchCommand();
                } else if (input.equals("switchchannel")) {
                    switchChannelCommand();
                } else if (input.equals("get")) {
                    getCommand();
                } else if (input.equals("reconnect")) {
                    reconnectCommand();
                } else if (input.equals("quit")) {
                    quitCommand();
                } else {
                    unknownCommand();
                }
            } else {
                String command = input.substring(0, input.indexOf(" "));
                String argument = input.substring(input.indexOf(" "), input.length());
                if (command.equals("dm")) {
                    dmCommand(argument);
                } else if (command.equals("get")) {
                    getWithArgumentCommand(argument);
                } else if (command.equals("send")) {
                    sendWithArgumentCommand(argument);
                } else {
                    unknownCommand();
                }
            }
        }
        System.out.println("Exiting manual control.");
        System.out.println(SEPERATOR);
        return;
    }
}
