package unrelibrary;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.HexFormat;

import unrelibrary.discordobjects.ApplicationCommand;
import unrelibrary.discordobjects.interactions.Interaction;
import unrelibrary.discordobjects.interactions.SlashCommandInteraction;
import unrelibrary.formatting.GeneralFormatter;
import unrelibrary.restapi.RESTManager;
import unrelibrary.restapi.ServerResponseException;
import unrelibrary.restapi.SlashCommand;
import unrelibrary.gatewayapi.EventReceiver;
import unrelibrary.gatewayapi.GatewayManager;

// this is the top class for interacting with both partitions of the discord api
public class APICommunicationManager {
    private final int PORT;
    private final int API_VERSION;
    private final String API_PREFIX;
    private final String TOKEN;
    private final String PUBLIC_KEY; // is this necessary?
    private final byte[] PUBLIC_KEY_BYTES;
    private final long APPLICATION_ID;
    private final boolean VERBOSE;
    private final DiscordBot DISCORD_BOT;
    private final EventReceiver EVENT_RECEIVER;
    
    private Map<String, SlashCommand> slashCommandNameRegistry = new TreeMap<String, SlashCommand>();
    private long ownID; // id of the own bot. readable with getOwnID
    
    public RESTManager restManager; // public for when the user wants to do something specific. it would be a shame (and work!) to lock this away in wrappers
    public GatewayManager gatewayManager; // these two managers also communicate with each other on specific occasions.

    public APICommunicationManager(int port, int apiVersion, String token, String publicKey, long applicationID, DiscordBot discordBot, boolean verbose, EventReceiver eventReceiver) {
        this.PORT = port;
        this.API_VERSION = apiVersion;
        this.API_PREFIX = "https://discord.com/api/v" + apiVersion + "/";
        this.TOKEN = token;
        this.PUBLIC_KEY = publicKey;
        this.PUBLIC_KEY_BYTES = HexFormat.of().parseHex(publicKey);
        this.APPLICATION_ID = applicationID;
        this.DISCORD_BOT = discordBot;
        this.VERBOSE = verbose;
        this.EVENT_RECEIVER = eventReceiver;
    }

    public long getOwnID() {
        return ownID;
    }

    // hands the signal on to manual control. this feels a little bit weird. is this normal in the world of java?
    public void gatewayConnected() {
        DISCORD_BOT.gatewayConnected();
        return;
    }

    // only use this in critical cases. as of now, this is if the gateway invalidates the session.
    public boolean goOffline() {
        return DISCORD_BOT.goOffline();
    }

    public boolean registerSlashCommand(String name, SlashCommand slashCommand) {
        for (SlashCommand registeredSlashCommand : slashCommandNameRegistry.values()) {
            if (registeredSlashCommand.name.equals(slashCommand.name)) {
                GeneralFormatter.printlnIfVerbose("Can't create slash command on discord because there is already a slash command under the name \"" + name + "\".", VERBOSE);
                return false;
            }
        }
        slashCommandNameRegistry.put(name, slashCommand);
        GeneralFormatter.printlnIfVerbose("Successfully created or overwrote local command \"" + name + "\"", VERBOSE);
        return true;
    }

    public boolean deleteSlashCommand(String name) {
        if (!slashCommandNameRegistry.containsKey(name)) {
            GeneralFormatter.printlnIfVerbose("Can't delete slash command that was never declared", VERBOSE);
            return false;
        } else {
            slashCommandNameRegistry.remove(name);
            GeneralFormatter.printlnIfVerbose("Successfully removed local command \"" + name + "\"", VERBOSE);
            return true;
        }
    }

    public boolean syncSlashCommands() {
        int slashCommandsCreatedOnline = 0;
        int slashCommandsUpdatedOnline = 0;
        int slashCommandsDeletedOnline = 0;
        Map<String, Boolean> localExistsOnline = new TreeMap<String, Boolean>(); // this is for verifying that everything local exists online too
        for (String slashCommandName : slashCommandNameRegistry.keySet()) {
            localExistsOnline.put(slashCommandName, Boolean.valueOf(false)); // we will look later which values remained false
        }
        ApplicationCommand[] allSlashCommandsOnline;
        try {
            allSlashCommandsOnline = restManager.getAllSlashCommands();
        } catch (ServerResponseException serverResponseException) {
            GeneralFormatter.printException("Couldn't get information about slash commands known online.", serverResponseException);
            return false;
        }
        for (ApplicationCommand slashCommandOnline : allSlashCommandsOnline) {
            if (!slashCommandNameRegistry.containsKey(slashCommandOnline.NAME)) {
                // this slash command doesn't exist locally; delete it
                try {
                    restManager.removeSlashCommand(slashCommandOnline.NAME);
                    slashCommandsDeletedOnline++; 
                } catch (ServerResponseException serverResponseException) {
                    GeneralFormatter.printException("Couldn't delete slash command \"" + slashCommandOnline.NAME + "\" from online interface.", serverResponseException);
                    return false;
                }
            } else {
                // this slash command exists online; check whether its structure matches
                // it seems that for registering a slash command object with no options, we need to pass [], but when we receive one, we get null instead of []
                // i'll just assume that [] and null are supposed to be used interchangably
                SlashCommand slashCommandLocal = slashCommandNameRegistry.get(slashCommandOnline.NAME);
                boolean identicalStructure = true; // base value
                boolean localNoOptions = false;
                boolean onlineNoOptions = false;
                if (slashCommandLocal.options == null) {
                    localNoOptions = true;
                } else if (slashCommandLocal.options.length == 0) {
                    localNoOptions = true;
                }
                if (slashCommandOnline.OPTIONS == null) {
                    onlineNoOptions = true;
                } else if (slashCommandOnline.OPTIONS.length == 0) {
                    onlineNoOptions = true;
                }
                if (localNoOptions || onlineNoOptions) {
                    if (localNoOptions && onlineNoOptions) {
                        identicalStructure = true;
                    } else {
                        identicalStructure = false;
                    }
                } else {
                    if (slashCommandLocal.options.length == slashCommandOnline.OPTIONS.length) {
                        for (int i = 0; i < slashCommandLocal.options.length; i++) {
                            if (!slashCommandLocal.options[i].name.equals(slashCommandOnline.OPTIONS[i].NAME)
                                || !slashCommandLocal.options[i].description.equals(slashCommandOnline.OPTIONS[i].DESCRIPTION)
                                || slashCommandLocal.options[i].type != slashCommandOnline.OPTIONS[i].TYPE
                            ) {
                                identicalStructure = false;
                                break;
                            }
                        }
                    } else {
                        identicalStructure = false;
                    }
                }
                if (slashCommandLocal.contexts != null || slashCommandOnline.CONTEXTS != null) {
                    if (slashCommandLocal.contexts != null && slashCommandOnline.CONTEXTS != null) {
                        if (slashCommandLocal.contexts.length != slashCommandOnline.CONTEXTS.length) {
                            identicalStructure = false;
                        } else {
                            for (int i = 0; i < slashCommandLocal.contexts.length; i++) {
                                if (slashCommandLocal.contexts[i] != slashCommandOnline.CONTEXTS[i]) {
                                    identicalStructure = false;
                                }
                            }
                        }
                    } else {
                        identicalStructure = false;
                    }
                }
                if (!identicalStructure) {
                    try {
                        restManager.registerSlashCommand(slashCommandLocal);
                        slashCommandsUpdatedOnline++;
                    } catch (ServerResponseException serverResponseException) {
                        GeneralFormatter.printException("Couldn't overwrite slash command \"" + slashCommandLocal.name + "\" of the online interface.", serverResponseException);
                        return false;
                    }
                }
                localExistsOnline.put(slashCommandOnline.NAME, true);
            }
        }
        // check whether we need to register any new commands online entirely
        for (Map.Entry<String, Boolean> slashCommandStatus : localExistsOnline.entrySet()) {
            if (slashCommandStatus.getValue() == false) {
                // register the command online
                try {
                    restManager.registerSlashCommand(slashCommandNameRegistry.get(slashCommandStatus.getKey()));
                    slashCommandsCreatedOnline++;
                } catch (ServerResponseException serverResponseException) {
                    GeneralFormatter.printException("Couldn't add slash command \"" + slashCommandNameRegistry.get(slashCommandStatus.getKey()).name + "\" of the online interface.", serverResponseException);
                    return false;
                }
            }
        }
        GeneralFormatter.printlnIfVerbose("Synced slash command displayed online to the local information: " + slashCommandsCreatedOnline + " created, " + slashCommandsUpdatedOnline + " updated, " + slashCommandsDeletedOnline + " deleted.", VERBOSE);
        return true;
    }

    public Interaction.CustomIDUpdatingResponse callSlashCommand(SlashCommandInteraction slashCommandInteraction) {
        if (slashCommandNameRegistry.containsKey(slashCommandInteraction.DATA.NAME)) {
            return slashCommandNameRegistry.get(slashCommandInteraction.DATA.NAME).action.apply(slashCommandInteraction);
        } else {
            GeneralFormatter.printlnIfVerbose("There is no slash command with name " + slashCommandInteraction.DATA.NAME + ".", VERBOSE);
            return null;
        }
    }

    public boolean setUpREST() {
        GeneralFormatter.printlnIfVerbose("Setting up HTTP server to the REST API...", VERBOSE);
        try {
            restManager = new RESTManager(this, PORT, API_VERSION, API_PREFIX, TOKEN, PUBLIC_KEY, PUBLIC_KEY_BYTES, APPLICATION_ID);
        } catch (IOException ioException) {
            GeneralFormatter.printException("An i/o exception occured while preparing the REST connection.", ioException);
            return false;
        }
        GeneralFormatter.printlnIfVerbose("Syncing slash commands...", VERBOSE);
        if (!syncSlashCommands()) { // verbosity is already handled in this method
            return false;
        }
        GeneralFormatter.printlnIfVerbose("Getting own ID...", VERBOSE);
        try {
            ownID = restManager.getCurrentUser().ID;
        } catch (ServerResponseException serverResponseException) {
            GeneralFormatter.printException("Couldn't get own user ID.", serverResponseException);
            return false;
        }
        return true;   
    }

    public boolean setUpGateway() {
        GeneralFormatter.printlnIfVerbose("Getting Data about the gateway...", VERBOSE);
        gatewayManager = new GatewayManager(this, API_VERSION, TOKEN, VERBOSE, EVENT_RECEIVER);
        try {
            gatewayManager.getGatewayData();
        } catch (ServerResponseException serverResponseException) {
            GeneralFormatter.printException("Couldn't get data about the gateway.", serverResponseException);
            return false;
        }
        gatewayManager.printGatewayAllowance(); // there are time-based limits
        GeneralFormatter.printlnIfVerbose("Opening the websocket connection to discord...", VERBOSE);
        gatewayManager.openWebsocket();
        return true;
    }

    public boolean disconnectREST() {
        GeneralFormatter.printlnIfVerbose("Shutting down the HTTP server...", VERBOSE);
        restManager.shutdownHTTPServer();
        return true;
    }

    public boolean disconnectGateway() {
        GeneralFormatter.printlnIfVerbose("Shutting down the HeartbeatSender...", VERBOSE);
        gatewayManager.stopHeartbeats();
        GeneralFormatter.printlnIfVerbose("Shutting down the websocket...", VERBOSE);
        gatewayManager.closeWebsocket();
        return true;
    }
}
