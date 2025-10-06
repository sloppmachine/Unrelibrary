package unrelibrary.restapi;

import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.HexFormat;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.InetSocketAddress;

import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.crypto.signers.Ed25519Signer;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import unrelibrary.APICommunicationManager;
import unrelibrary.discordobjects.ApplicationCommand;
import unrelibrary.discordobjects.Channel;
import unrelibrary.discordobjects.Guild;
import unrelibrary.discordobjects.GuildMember;
import unrelibrary.discordobjects.GuildThreads;
import unrelibrary.discordobjects.Message;
import unrelibrary.discordobjects.User;
import unrelibrary.discordobjects.components.Component;
import unrelibrary.discordobjects.interactions.ComponentInteraction;
import unrelibrary.discordobjects.interactions.Interaction;
import unrelibrary.discordobjects.interactions.ModalInteraction;
import unrelibrary.discordobjects.interactions.SlashCommandInteraction;
import unrelibrary.formatting.GeneralFormatter;
import unrelibrary.formatting.JSONBuilder;
import unrelibrary.formatting.JSONFactory;

// this is for managing the http communication with the api
public class RESTManager {
     // some of these values aren't ever used by the rest api, but i still keep them for completeness and aesthetics, and because having only some would be very confusing to me
    private final int PORT;
    private final int API_VERSION;
    private final String API_PREFIX;
    private final String TOKEN;
    private final String PUBLIC_KEY;
    private final byte[] PUBLIC_KEY_BYTES;
    private final long APPLICATION_ID;
    private final APICommunicationManager apiCommunicationManager;
    private HttpServer httpServer;
    private HttpClient httpClient = HttpClient.newHttpClient();
    private CustomHandler customHandler;

    // functions that are called when a component is interacted with can be registered with their custom id, and can be removed again by returning a true boolean after interacting
    private Map<String, Function<ComponentInteraction, Interaction.CustomIDUpdatingResponse>> componentNotificationCustomIDs
        = new TreeMap<String, Function<ComponentInteraction, Interaction.CustomIDUpdatingResponse>>();
    private Map<String, Function<ModalInteraction, Interaction.CustomIDUpdatingResponse>> modalNotificationCustomIDs
        = new TreeMap<String, Function<ModalInteraction, Interaction.CustomIDUpdatingResponse>>();

    public RESTManager(
        APICommunicationManager outerApiCommunicationManager,
        int port,
        int apiVersion,
        String apiPrefix,
        String token,
        String publicKey,
        byte[] publicKeyBytes,
        long applicationID
    ) throws IOException {
        this.apiCommunicationManager = outerApiCommunicationManager;
        this.PORT = port;
        this.API_VERSION = apiVersion;
        this.API_PREFIX = apiPrefix;
        this.TOKEN = token;
        this.PUBLIC_KEY = publicKey;
        this.PUBLIC_KEY_BYTES = HexFormat.of().parseHex(PUBLIC_KEY);
        this.APPLICATION_ID = applicationID;
        this.httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);
        this.customHandler = new CustomHandler();
        this.httpServer.createContext("/bot", this.customHandler);
        this.httpServer.setExecutor(null);
        this.httpServer.start();
    }

    // this class handles messages from the discord api; as of now this includes only server pings and slash commands.
    private class CustomHandler implements HttpHandler {

        // this is called for each message the handler receives
        public void handle(HttpExchange httpExchange) throws IOException {
            Headers receivedHeaders = httpExchange.getRequestHeaders();
            String receivedBody = new String(httpExchange.getRequestBody().readAllBytes());
            // first of all, the request needs to validated (does it really come from discord?)
            String signatureString = receivedHeaders.get("X-signature-ed25519").get(0);
            String timeStampString = receivedHeaders.get("X-signature-timestamp").get(0);
            if (validateRequest(signatureString, timeStampString, receivedBody)) {
                Integer receivedType = JSONFactory.getDiscordInteractionType(receivedBody);
                if (receivedType == null) {
                    
                } else if (receivedType.intValue() == 1) {
                    respondToInteraction(httpExchange, 200, "{\"type\": 1}"); // respond with a simple type 1
                } else if (receivedType.intValue() == 2) {
                    SlashCommandInteraction slashCommandInteraction = JSONFactory.jsonToDiscordSlashCommandInteraction(receivedBody);
                    
                    Interaction.CustomIDUpdatingResponse customIDUpdatingResponse = apiCommunicationManager.callSlashCommand(slashCommandInteraction);
                    if (customIDUpdatingResponse != null) {
                        respondToInteraction(httpExchange, 200, customIDUpdatingResponse.response.toJSON());
                        updateCustomIDs(customIDUpdatingResponse.customIDListeningUpdate);
                    }
                } else if (receivedType.intValue() == 3) {
                    ComponentInteraction componentInteraction = JSONFactory.jsonToDiscordComponentInteraction(receivedBody);
                    if (componentNotificationCustomIDs.containsKey(componentInteraction.DATA.CUSTOM_ID)) {
                        Interaction.CustomIDUpdatingResponse customIDUpdatingResponse = componentNotificationCustomIDs.get(componentInteraction.DATA.CUSTOM_ID).apply(componentInteraction);
                        respondToInteraction(httpExchange, 200, customIDUpdatingResponse.response.toJSON());
                        updateCustomIDs(customIDUpdatingResponse.customIDListeningUpdate);
                    }
                } else if (receivedType.intValue() == 5) {
                    // this means that someone has filled out a modal's components. annoyingly, these interactions contain just the same information as a set of
                    // component interactions would. we will just extract component interactions out of the modal interaction, then proceed as usual.
                    ModalInteraction modalInteraction = JSONFactory.jsonToDiscordModalInteraction(receivedBody);
                    if (modalNotificationCustomIDs.containsKey(modalInteraction.DATA.CUSTOM_ID)) {
                        Interaction.CustomIDUpdatingResponse customIDUpdatingResponse = modalNotificationCustomIDs.get(modalInteraction.DATA.CUSTOM_ID).apply(modalInteraction);
                        respondToInteraction(httpExchange, 200, customIDUpdatingResponse.response.toJSON());
                        updateCustomIDs(customIDUpdatingResponse.customIDListeningUpdate);
                    }
                }
            } else {
                // discord sends "fake" requests to check that you are validating the requests.
                // i do not know whether you can fail this test by just answering alone. but we dont have a reason to respond to anything else anyway.
            }
            return;
        }

        // this checks very a request really comes from discord. it uses a real security library instead of my voodoo
        private boolean validateRequest(String signatureString, String timestampString, String body) {
            try {
                byte[] signatureBytes = HexFormat.of().parseHex(signatureString);
                byte[] bodyBytes = body.getBytes();
                byte[] timestampBytes = timestampString.getBytes(); 
                // the bytes of the body and the timestamp are supposed to be appended to be checked
                byte[] bodyAndTimestamp = new byte[timestampBytes.length + bodyBytes.length];
                System.arraycopy(timestampBytes, 0, bodyAndTimestamp, 0, timestampBytes.length);
                System.arraycopy(bodyBytes, 0, bodyAndTimestamp, timestampBytes.length, bodyBytes.length);
                // rather use a dedicated security library than do this myself
                Ed25519PublicKeyParameters pubKeyParams = new Ed25519PublicKeyParameters(PUBLIC_KEY_BYTES, 0);
                Ed25519Signer verifier = new Ed25519Signer();
                verifier.init(false, pubKeyParams); // the false indicates we want to verify a signature
                verifier.update(bodyAndTimestamp, 0, bodyAndTimestamp.length);
                return verifier.verifySignature(signatureBytes);
            } catch (Exception exception) {
                GeneralFormatter.printException("An exception occured while validating a request.", exception);
                return false;
            }
        }

        // general method for responding to a discord interaction
        private void respondToInteraction(HttpExchange interaction, int responseCode, String jsonBody) throws IOException {
            byte[] jsonBodyBytes = jsonBody.getBytes();
            interaction.getResponseHeaders().set("Content-Type", "application/json");
            interaction.sendResponseHeaders(responseCode, jsonBodyBytes.length);
            OutputStream outputStream = interaction.getResponseBody();
            outputStream.write(jsonBodyBytes);
            outputStream.close();
            return;
        }
    }

    // updates the custom IDs to listen to.
    // if an id is both a part of stop listening and start listening, it will continue to be listened to
    private void updateCustomIDs(CustomIDListeningUpdate customIDListeningUpdate) {
        if (customIDListeningUpdate != null) {
            if (customIDListeningUpdate.COMPONENT_NOTIFICATION_STOP_LISTENING != null) {
                for (String customID : customIDListeningUpdate.COMPONENT_NOTIFICATION_STOP_LISTENING) {
                    componentNotificationCustomIDs.remove(customID);
                }
            }
            if (customIDListeningUpdate.MODAL_NOTIFICATION_STOP_LISTENING != null) {
                for (String customID : customIDListeningUpdate.MODAL_NOTIFICATION_STOP_LISTENING) {
                    modalNotificationCustomIDs.remove(customID);
                }
            }
            if (customIDListeningUpdate.COMPONENT_NOTIFICATION_START_LISTENING != null) {
                if (!customIDListeningUpdate.COMPONENT_NOTIFICATION_START_LISTENING.isEmpty()) {
                    componentNotificationCustomIDs.putAll(customIDListeningUpdate.COMPONENT_NOTIFICATION_START_LISTENING);
                }
            }
            if (customIDListeningUpdate.MODAL_NOTIFICATION_START_LISTENING != null) {
                if (!customIDListeningUpdate.MODAL_NOTIFICATION_START_LISTENING.isEmpty()) {
                    modalNotificationCustomIDs.putAll(customIDListeningUpdate.MODAL_NOTIFICATION_START_LISTENING);
                }
            }
        }
        return;
    }

    public void shutdownHTTPServer() {
        httpServer.stop(0);
        return;
    }

    // overloading for making the last parameters optional
    public String request(String URLname, String method) throws ServerResponseException {
        return request(URLname, method, null, null);
    }

    public String request(String URLname, String method, String jsonBody) throws ServerResponseException {
        return request(URLname, method, jsonBody, null);
    }
    
    // actual method for sending a simple http request
    public String request(String URLname, String method, String jsonBody, Map<String, String> additionalQueries) throws ServerResponseException { // fix the return type later.
        try {
            if (additionalQueries != null) {
                URLname += "?";
                for (Map.Entry<String, String> query : additionalQueries.entrySet()) {
                    URLname += query.getKey() + "=" + query.getValue() + "&";
                }
                URLname = URLname.substring(0, URLname.length() - 1); // remove the final &
            }
            if (jsonBody == null) {
                jsonBody = "";
            }
            HttpRequest httpRequest = HttpRequest.newBuilder().uri(new URI(URLname))
                .method(method, BodyPublishers.ofString(jsonBody))
                .header("User-Agent", "DiscordBot (https://discord.com/oauth2/authorize?client_id=1397622652046414017, 0)")
                .header("Authorization", "Bot " + TOKEN)
                .header("Content-Type", "application/json")
                .build();
            HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            int responseCode = httpResponse.statusCode();
            if (responseCode >= 400) { // big number bad. errors have occured
                throw new ServerResponseException("Response code: " + responseCode + ". Server response: " + httpResponse.body());
            }
            return httpResponse.body();
        
        } catch (Exception exception) {
            GeneralFormatter.printException("An exception occured while sending an HTTP request.", exception);
            return "";
        }
    }

    public User getCurrentUser() throws ServerResponseException {
        return JSONFactory.jsonToDiscordUser(request(API_PREFIX + "users/@me", "GET"));
    }

    public User getUser(long id) throws ServerResponseException {
        return JSONFactory.jsonToDiscordUser(request(API_PREFIX + "users/" + id, "GET"));
    }

    public Guild[] getJoinedGuilds() throws ServerResponseException {
        return JSONFactory.jsonArrayToDiscordGuilds(request(API_PREFIX + "users/@me/guilds", "GET"));
    }

    public Channel[] getGuildChannels(long guildID) throws ServerResponseException {
        return JSONFactory.jsonArrayToDiscordChannels(request(API_PREFIX + "guilds/" + guildID + "/channels", "GET"));
    }

    public GuildThreads getGuildThreads(long guildID) throws ServerResponseException {
        return JSONFactory.jsonToDiscordGuildThreads(request(API_PREFIX + "guilds/" + guildID + "/threads/active", "GET"));
    }

    public GuildMember getGuildMember(long guildID, long userID) throws ServerResponseException {
        return JSONFactory.jsonToDiscordGuildMember(request(API_PREFIX + "guilds/" + guildID + "/members/" + userID, "GET"), guildID);
    }

    // sends a massage to a discord channel, returns the server response
    public String sendMessage(long channelID, String content) throws ServerResponseException {
        JSONBuilder jsonBodyBuilder = new JSONBuilder();
        jsonBodyBuilder.addStringProperty("content", content);
        jsonBodyBuilder.addBooleanProperty("tts", false);
        String jsonBody = jsonBodyBuilder.build();
        return request(API_PREFIX + "channels/" + channelID + "/messages", "POST", jsonBody);
    }

    public String editComponentMessage(long channelID, long messageID, Component[] components) throws ServerResponseException {
        JSONBuilder jsonBodyBuilder = new JSONBuilder();
        jsonBodyBuilder.addIntProperty("flags",  (int) Math.pow(2, 15)); // for components
        jsonBodyBuilder.addLiteralProperty("component", JSONBuilder.buildArray(components));
        return request(API_PREFIX + "channels/" + channelID + "/messages/" + messageID, "PATCH", jsonBodyBuilder.build());
    }

    public String deleteMessage(long channelID, long messageID) throws ServerResponseException {
        return request(API_PREFIX + "channels/" + channelID + "/messages/" + messageID, "DELETE");
    }

    public Channel getChannel(long channelID) throws ServerResponseException {
        return JSONFactory.jsonToDiscordChannel(request(API_PREFIX + "channels/" + channelID, "GET"));
    }

    // finds the dm channel connecting the bot to a specific user, returns the discordchannel object
    public Channel getDMChannel(long userID) throws ServerResponseException {
        JSONBuilder jsonBodyBuilder = new JSONBuilder();
        jsonBodyBuilder.addStringProperty("recipient_id", String.valueOf(userID));
        String jsonBody = jsonBodyBuilder.build();
        return JSONFactory.jsonToDiscordChannel(request(API_PREFIX + "users/@me/channels", "POST", jsonBody));
    }

    public Message[] getLastMessages(long channelID, int amount) throws ServerResponseException {
        if (amount <= 0) {
            amount = 1;
        }
        Map<String, String> additionalQueries = new TreeMap<String, String>();
        additionalQueries.put("limit", Integer.valueOf(amount).toString());
        return JSONFactory.jsonArrayToDiscordMessages(request(API_PREFIX + "channels/" + channelID + "/messages", "GET", null, additionalQueries));
    }

    // this creates a new slash command that is known to discord and its users.
    public String registerSlashCommand(SlashCommand slashCommand) throws ServerResponseException {
        return request(API_PREFIX + "applications/" + APPLICATION_ID + "/commands", "POST", slashCommand.toJSON());
    }

    // this returns an array of all application commands currently visible online
    public ApplicationCommand[] getAllApplicationCommands() throws ServerResponseException {
        return JSONFactory.jsonArrayToDiscordApplicationCommands(request(API_PREFIX + "applications/" + APPLICATION_ID + "/commands", "GET"));
    }

    // slash commands are a subset of application commands
    public ApplicationCommand[] getAllSlashCommands() throws ServerResponseException {
        ApplicationCommand[] allApplicationCommands = getAllApplicationCommands();
        List<ApplicationCommand> toReturnBuilder = new LinkedList<ApplicationCommand>();
        for (ApplicationCommand discordApplicationCommand : allApplicationCommands) {
            if (discordApplicationCommand.TYPE == 1) {
                toReturnBuilder.add(discordApplicationCommand);
            }
        }
        return toReturnBuilder.toArray(new ApplicationCommand[toReturnBuilder.size()]);
    }

    // "removing" here means remove the command from the discord app, so that users online can't see or use it
    public String removeSlashCommand(String name) throws ServerResponseException {
        ApplicationCommand[] allSlashCommands = getAllSlashCommands();
        for (ApplicationCommand slashCommand : allSlashCommands) {
            if (slashCommand.NAME.equals(name)) {
                return request(API_PREFIX + "/applications/" + APPLICATION_ID + "/commands/" + slashCommand.ID, "DELETE");
            }
        }
        return null;
    }

    // for the gateway api
    public String getGatewayData() throws ServerResponseException {
        return request(API_PREFIX + "gateway/bot", "GET");
    }
}