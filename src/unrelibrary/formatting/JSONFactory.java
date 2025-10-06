package unrelibrary.formatting;

import java.util.Map;

import unrelibrary.MalformedException;
import unrelibrary.discordobjects.ApplicationCommand;
import unrelibrary.discordobjects.Channel;
import unrelibrary.discordobjects.GatewayData;
import unrelibrary.discordobjects.GatewayReadyEventData;
import unrelibrary.discordobjects.Guild;
import unrelibrary.discordobjects.GuildMember;
import unrelibrary.discordobjects.GuildThreads;
import unrelibrary.discordobjects.Message;
import unrelibrary.discordobjects.User;
import unrelibrary.discordobjects.events.GatewayDispatchEvent;
import unrelibrary.discordobjects.events.GatewayEvent;
import unrelibrary.discordobjects.events.GatewayHelloEvent;
import unrelibrary.discordobjects.events.GatewayInvalidSessionEvent;
import unrelibrary.discordobjects.interactions.ComponentInteraction;
import unrelibrary.discordobjects.interactions.ModalInteraction;
import unrelibrary.discordobjects.interactions.SlashCommandInteraction;

// this is for formatting messages between the local device and the api. in reality, this just means unpacking json files
public class JSONFactory extends GeneralFormatter {

    // these methods account for nullable fields
    public static Boolean extractBoolean(String string) {
        if (string == null) {
            return null;
        } else if (string.equals("null")) {
            return null;
        } else {
            return Boolean.valueOf(string);
        }
    }

    public static Integer extractInt(String string) {
        if (string == null) {
            return null;
        } else if (string.equals("null")) {
            return null;
        } else {
            return Integer.valueOf(string);
        }
    }

    // snowflakes represent longs but are passed on as strings
    public static Long extractLong(String string) {
        if (string == null) {
            return null;
        } else if (string.equals("null")) {
            return null;
        } else {
            return Long.valueOf(string.substring(1, string.length() - 1));
        }
    }

    public static String extractString(String string) {
        if (string == null) {
            return null;
        } else if (string.equals("null")) {
            return null;
        } else {
            return string.substring(1, string.length() - 1);
        }
    }

    public static int[] extractIntArray(String string) {
        if (string == null) {
            return null;
        } else if (string.equals("null")) {
            return null;
        } else {
            try {
                String[] rawSeparatedArray = separateArray(string);
                int[] toReturn = new int[rawSeparatedArray.length];
                for (int i = 0; i < rawSeparatedArray.length; i++) {
                    toReturn[i] = extractInt(rawSeparatedArray[i]);
                }
                return toReturn;
            } catch (MalformedException malformedException) {
                GeneralFormatter.printException("Couldn't produce an Integer array", malformedException);
                return null;
            }
        }
    }

    public static String[] extractStringArray(String string) {
        if (string == null) {
            return null;
        } else if (string.equals("null")) {
            return null;
        } else {
            try {
                String[] rawSeparatedArray = separateArray(string);
                String[] toReturn = new String[rawSeparatedArray.length];
                for (int i = 0; i < rawSeparatedArray.length; i++) {
                    toReturn[i] = extractString(rawSeparatedArray[i]);
                }
                return toReturn;
            } catch (MalformedException malformedException) {
                GeneralFormatter.printException("Couldn't produce a String array.", malformedException);
                return null;
            }
        }
    }

    // below methods are for building a discord object from json strings.
    public static SlashCommandInteraction jsonToDiscordSlashCommandInteraction(String string) {
        if (string == null) {
            return null;
        } else if (string.equals("{}")) {
            return null;
        } else {
            try {
                Map<String, String> rawSeparatedJSON = separateJSON(string);
                Map<String, String> rawSeparatedData = separateJSON(rawSeparatedJSON.get("\"data\""));
                SlashCommandInteraction toReturn = new SlashCommandInteraction(
                    extractLong(rawSeparatedJSON.get("\"id\"")),
                    extractInt(rawSeparatedJSON.get("\"type\"")),
                    extractString(rawSeparatedJSON.get("\"token\"")),
                    jsonToDiscordUser(rawSeparatedJSON.get("\"user\"")),
                    jsonToDiscordGuildMember(
                        rawSeparatedJSON.get("\"member\""),
                        extractLong(rawSeparatedJSON.get("\"guild_id\""))
                    ),
                    extractLong(rawSeparatedJSON.get("\"channel_id\"")),
                    new SlashCommandInteraction.Data(
                        extractString(rawSeparatedData.get("\"name\"")),
                        jsonArrayToDiscordSlashCommandInteractionDataOptions(rawSeparatedData.get("\"options\""))
                    )
                );
                return toReturn;
            } catch (MalformedException malformedException) {
                GeneralFormatter.printException("Couldn't produce a SlashCommandInteraction Object.", malformedException);
                return null;
            }
        }
    }

    public static SlashCommandInteraction.Data.Option jsonToDiscordSlashCommandInteractionDataOption(String string) {
        if (string == null) {
            return null;
        } else if (string.equals("{}")) {
            return null;
        } else {
            try {
                Map<String, String> rawSeparatedJSON = separateJSON(string);
                SlashCommandInteraction.Data.Option toReturn = new SlashCommandInteraction.Data.Option(
                    extractInt(rawSeparatedJSON.get("\"type\"")),
                    extractString(rawSeparatedJSON.get("\"name\"")),
                    extractString(rawSeparatedJSON.get("\"value\""))
                );
                return toReturn;
            } catch (MalformedException malformedException) {
                GeneralFormatter.printException("Couldn't produce a SlashCommandInteraction.Data.Option Object.", malformedException);
                return null;
            }
        }
    }

    public static ComponentInteraction jsonToDiscordComponentInteraction(String string) {
        if (string == null) {
            return null;
        } else if (string.equals("{}")) {
            return null;
        } else {
            try {
                Map<String, String> rawSeparatedJSON = separateJSON(string);
                Map<String, String> rawSeparatedData = separateJSON(rawSeparatedJSON.get("\"data\""));
                ComponentInteraction toReturn = new ComponentInteraction(
                    extractLong(rawSeparatedJSON.get("\"id\"")),
                    extractInt(rawSeparatedJSON.get("\"type\"")),
                    extractString(rawSeparatedJSON.get("\"token\"")),
                    jsonToDiscordUser(rawSeparatedJSON.get("\"user\"")),
                    jsonToDiscordGuildMember(
                        rawSeparatedJSON.get("\"member\""),
                        extractLong(rawSeparatedJSON.get("\"guild_id\""))
                    ),
                    extractLong(rawSeparatedJSON.get("\"channel_id\"")),
                    jsonToDiscordMessage(rawSeparatedJSON.get("\"message\"")),
                    new ComponentInteraction.Data(
                        extractString(rawSeparatedData.get("\"custom_id\"")),
                        extractInt(rawSeparatedData.get("\"component_type\"")),
                        extractStringArray(rawSeparatedData.get("\"values\"")),
                        extractString(rawSeparatedData.get("\"value\""))
                    )
                );
                return toReturn;
            } catch (MalformedException malformedException) {
                GeneralFormatter.printException("Couldn't produce a ComponentInteraction Object.", malformedException);
                return null;
            }
        }
    }

    public static ModalInteraction jsonToDiscordModalInteraction(String string) {
        if (string == null) {
            return null;
        } else if (string.equals("{}")) {
            return null;
        } else {
            try {
                Map<String, String> rawSeparatedJSON = separateJSON(string);
                Map<String, String> rawSeparatedData = separateJSON(rawSeparatedJSON.get("\"data\""));
                ModalInteraction toReturn = new ModalInteraction(
                    extractLong(rawSeparatedJSON.get("\"id\"")),
                    extractInt(rawSeparatedJSON.get("\"type\"")),
                    extractString(rawSeparatedJSON.get("\"token\"")),
                    jsonToDiscordUser(rawSeparatedJSON.get("\"user\"")),
                    jsonToDiscordGuildMember(
                        rawSeparatedJSON.get("\"member\""),
                        extractLong(rawSeparatedJSON.get("\"guild_id\""))
                    ),
                    extractLong(rawSeparatedJSON.get("\"channel_id\"")),
                    new ModalInteraction.Data(
                        extractString(rawSeparatedData.get("\"custom_id\"")),
                        jsonArrayToModalInteractionDataModalComponents(rawSeparatedData.get("\"components\""))
                    )
                );
                return toReturn;
            } catch (MalformedException malformedException) {
                GeneralFormatter.printException("Couldn't produce a ModalInteraction Object.", malformedException);
                return null;
            }
        }
    }

    public static ModalInteraction.Data.ModalComponent jsonArrayToModalInteractionDataModalComponent(String string) {
        if (string == null) {
            return null;
        } else if (string.equals("{}")) {
            return null;
        } else {
            try {
                Map<String, String> rawSeparatedJSON = separateJSON(string);
                Map<String, String> rawSeparatedComponent = separateJSON(rawSeparatedJSON.get("\"component\""));
                ModalInteraction.Data.ModalComponent toReturn = new ModalInteraction.Data.ModalComponent(
                    extractInt(rawSeparatedJSON.get("\"type\"")),
                    extractInt(rawSeparatedJSON.get("\"id\"")),
                    new ModalInteraction.Data.ModalComponent.ModalComponentSubmission(
                        extractInt(rawSeparatedComponent.get("\"type\"")),
                        extractInt(rawSeparatedComponent.get("\"id\"")),
                        extractString(rawSeparatedComponent.get("\"custom_id\"")),
                        extractStringArray(rawSeparatedComponent.get("\"values\"")),
                        extractString(rawSeparatedComponent.get("\"value\""))
                    )
                );
                return toReturn;
            } catch (MalformedException malformedException) {
                GeneralFormatter.printException("Couldn't produce a ModalInteraction Object.", malformedException);
                return null;
            }
        }
    }

    public static ModalInteraction.Data.ModalComponent jsonToDiscordModalInteractionDataModalComponent(String string) {
        if (string == null) {
            return null;
        } else if (string.equals("{}")) {
            return null;
        } else {
            try {
                Map<String, String> rawSeparatedJSON = separateJSON(string);
                Map<String, String> rawComponentJSON = separateJSON(rawSeparatedJSON.get("\"component\""));
                ModalInteraction.Data.ModalComponent toReturn = new ModalInteraction.Data.ModalComponent(
                    extractInt(rawSeparatedJSON.get("\"type\"")),
                    extractInt(rawSeparatedJSON.get("\"id\"")),
                    new ModalInteraction.Data.ModalComponent.ModalComponentSubmission(
                        extractInt(rawComponentJSON.get("\"type\"")),
                        extractInt(rawComponentJSON.get("\"id\"")),
                        extractString(rawComponentJSON.get("\"custom_id\"")),
                        extractStringArray(rawComponentJSON.get("\"values\"")),
                        extractString(rawComponentJSON.get("\"value\""))
                    )
                );
                return toReturn;
            } catch (MalformedException malformedException) {
                GeneralFormatter.printException("Couldn't produce a ModalInteraction Object.", malformedException);
                return null;
            }
        }
    }

    public static User jsonToDiscordUser(String string) {
        if (string == null) {
            return null;
        } else if (string.equals("{}")) {
            return null;
        } else {
            try {
                Map<String, String> rawSeparatedJSON = separateJSON(string);
                User toReturn = new User(
                    extractLong(rawSeparatedJSON.get("\"id\"")),
                    extractString(rawSeparatedJSON.get("\"username\"")),
                    extractString(rawSeparatedJSON.get("\"global_name\"")),
                    extractString(rawSeparatedJSON.get("\"avatar\""))
                );
                return toReturn;
            } catch (MalformedException malformedException) {
                GeneralFormatter.printException("Couldn't produce a User Object.", malformedException);
                return null;
            }
        }
    }

    public static Guild jsonToDiscordGuild(String string) {
        if (string == null) {
            return null;
        } else if (string.equals("{}")) {
            return null;
        } else {
            try {
                Map<String, String> rawSeparatedJSON = separateJSON(string);
                Guild toReturn = new Guild(
                    extractLong(rawSeparatedJSON.get("\"id\"")),
                    extractString(rawSeparatedJSON.get("\"name\""))
                );
                return toReturn;
            } catch (MalformedException malformedException) {
                GeneralFormatter.printException("Couldn't produce a Guild Object.", malformedException);
                return null;
            }
        }
    }

    // discord doesn't send the guild id which a member belongs to. we need to instead keep track of it by passing it as an argument
    public static GuildMember jsonToDiscordGuildMember(String string, Long guildID) {
        if (string == null) {
            return null;
        } else if (string.equals("{}")) {
            return null;
        } else if (guildID == null) {
            return null;
        } else {
            try {
                Map<String, String> rawSeparatedJSON = separateJSON(string);
                GuildMember toReturn = new GuildMember(
                    guildID,
                    jsonToDiscordUser(rawSeparatedJSON.get("\"user\"")),
                    extractString(rawSeparatedJSON.get("\"nick\"")),
                    extractString(rawSeparatedJSON.get("\"avatar\""))
                );
                return toReturn;
            } catch (MalformedException malformedException) {
                GeneralFormatter.printException("Couldn't produce a GuildMember Object.", malformedException);
                return null;
            }
        }
    }

    public static Message jsonToDiscordMessage(String string) {
        if (string == null) {
            return null;
        } else if (string.equals("{}")) {
            return null;
        } else {
            try {
                Map<String, String> rawSeparatedJSON = separateJSON(string);
                Message toReturn = new Message(
                    extractLong(rawSeparatedJSON.get("\"id\"")),
                    jsonToDiscordUser(rawSeparatedJSON.get("\"author\"")),
                    extractString(rawSeparatedJSON.get("\"content\"")),
                    extractLong(rawSeparatedJSON.get("\"channel_id\""))
                );
                return toReturn;
            }  catch (MalformedException malformedException) {
                GeneralFormatter.printException("Couldn't produce a Messafe Object.", malformedException);
                return null;
            }
        }
    }

    public static Channel jsonToDiscordChannel(String string) {
        if (string == null) {
            return null;
        } else if (string.equals("{}")) {
            return null;
        } else {
            try {
                Map<String, String> rawSeparatedJSON = separateJSON(string);
                Channel toReturn = new Channel(
                    extractLong(rawSeparatedJSON.get("\"id\"")),
                    extractInt(rawSeparatedJSON.get("\"type\"")),
                    extractString(rawSeparatedJSON.get("\"name\"")),
                    jsonArrayToDiscordUsers(rawSeparatedJSON.get("\"recipients\""))
                );
                return toReturn;
            } catch (MalformedException malformedException) {
                GeneralFormatter.printException("Couldn't produce a Channel Object.", malformedException);
                return null;
            }
        }
    }
    public static GuildThreads jsonToDiscordGuildThreads(String string) {
        if (string == null) {
            return null;
        } else if (string.equals("{}")) {
            return null;
        } else {
            try {
                Map<String, String> rawSeparatedJSON = separateJSON(string);
                GuildThreads toReturn = new GuildThreads(
                    jsonArrayToDiscordChannels(rawSeparatedJSON.get("\"threads\""))
                );
                return toReturn;
            } catch (MalformedException malformedException) {
                GeneralFormatter.printException("Couldn't produce a GuildThreads Object.", malformedException);
                return null;
            }
        }
    }

    public static ApplicationCommand jsonToDiscordApplicationCommand(String string) {
        if (string == null) {
            return null;
        } else if (string.equals("{}")) {
            return null;
        } else {
            try {
                Map<String, String> rawSeparatedJSON = separateJSON(string);
                ApplicationCommand toReturn = new ApplicationCommand(
                    extractLong(rawSeparatedJSON.get("\"id\"")),
                    extractInt(rawSeparatedJSON.get("\"type\"")),
                    extractString(rawSeparatedJSON.get("\"name\"")),
                    extractString(rawSeparatedJSON.get("\"description\"")),
                    jsonArrayToDiscordApplicationCommandOptions(rawSeparatedJSON.get("\"options\"")),
                    extractIntArray(rawSeparatedJSON.get("\"contexts\""))
                );
                return toReturn;
            } catch (MalformedException malformedException) {
                GeneralFormatter.printException("Couldn't produce a ApplicationCommand Object.", malformedException);
                return null;
            }
        }
    }

    public static GatewayData jsonToDiscordGatewayData(String string) {
        if (string == null) {
            return null;
        } else if (string.equals("{}")) {
            return null;
        } else {
            try {
                Map<String, String> rawSeparatedJSON = separateJSON(string);
                GatewayData toReturn = new GatewayData(
                    extractString(rawSeparatedJSON.get("\"url\"")),
                    jsonToDiscordGatewayDataSessionStartLimit(rawSeparatedJSON.get("\"session_start_limit\""))
                );
                return toReturn;
            } catch (MalformedException malformedException) {
                GeneralFormatter.printException("Couldn't produce a GatewayData Object.", malformedException);
                return null;
            }
        }
    }

    public static Integer getDiscordInteractionType(String string) {
        try {
            Map<String, String> rawSeparatedJSON = separateJSON(string);
            String rawOp = rawSeparatedJSON.get("\"type\"");
            if (rawOp == null) {
                return null;
            } else {
                return Integer.valueOf(rawOp);
            }
        } catch (MalformedException malformedException) {
            GeneralFormatter.printException("Couldn't get the type of this Discord Interaction Object.", malformedException);
            return null;
        }
    }

    public static GatewayEvent jsonToDiscordGatewayEvent(String string) {
        if (string == null) {
            return null;
        } else if (string.equals("{}")) {
            return null;
        } else {
            try {
                Map<String, String> rawSeparatedJSON = separateJSON(string);
                GatewayEvent toReturn = new GatewayEvent(
                    extractInt(rawSeparatedJSON.get("\"op\"")),
                    extractInt(rawSeparatedJSON.get("\"s\"")),
                    extractString(rawSeparatedJSON.get("\"t\""))
                );
                return toReturn;
            } catch (MalformedException malformedException) {
                GeneralFormatter.printException("Couldn't produce a GatewayDispatchEvent Object.", malformedException);
                return null;
            }
        }
    }

    public static GatewayDispatchEvent jsonToDiscordGatewayDispatchEvent(String string) {
        if (string == null) {
            return null;
        } else if (string.equals("{}")) {
            return null;
        } else {
            try {
                Map<String, String> rawSeparatedJSON = separateJSON(string);
                GatewayDispatchEvent toReturn = new GatewayDispatchEvent(
                    extractInt(rawSeparatedJSON.get("\"s\"")),
                    extractString(rawSeparatedJSON.get("\"t\"")),
                    new GatewayDispatchEvent.Data(
                        rawSeparatedJSON.get("\"d\""),
                        separateJSON(rawSeparatedJSON.get("\"d\""))
                    )
                );
                return toReturn;
            } catch (MalformedException malformedException) {
                GeneralFormatter.printException("Couldn't produce a GatewayDispatchEvent Object.", malformedException);
                return null;
            }
        }
    }

    public static GatewayInvalidSessionEvent jsonToDiscordGatewayInvalidSessionEvent(String string) {
        if (string == null) {
            return null;
        } else if (string.equals("{}")) {
            return null;
        } else {
            try {
                Map<String, String> rawSeparatedJSON = separateJSON(string);
                GatewayInvalidSessionEvent toReturn = new GatewayInvalidSessionEvent(
                    extractInt(rawSeparatedJSON.get("\"s\"")),
                    extractString(rawSeparatedJSON.get("\"t\"")),
                    extractBoolean(rawSeparatedJSON.get("\"d\""))
                );
                return toReturn;
            } catch (MalformedException malformedException) {
                GeneralFormatter.printException("Couldn't produce a GatewayInvalidSessionEvent Object.", malformedException);
                return null;
            }
        }
    }

    public static GatewayHelloEvent jsonToDiscordGatewayHelloEvent(String string) {
        if (string == null) {
            return null;
        } else if (string.equals("{}")) {
            return null;
        } else {
            try {
                Map<String, String> rawSeparatedJSON = separateJSON(string);
                GatewayHelloEvent toReturn = new GatewayHelloEvent(
                    extractInt(rawSeparatedJSON.get("\"s\"")),
                    extractString(rawSeparatedJSON.get("\"t\"")),
                    jsonToDiscordGatewayHelloEventData(rawSeparatedJSON.get("\"d\""))
                );
                return toReturn;
            } catch (MalformedException malformedException) {
                GeneralFormatter.printException("Couldn't produce a GatewayHelloEvent Object.", malformedException);
                return null;
            }
        }
    }

    public static GatewayHelloEvent.Data jsonToDiscordGatewayHelloEventData(String string) {
        if (string == null) {
            return null;
        } else if (string.equals("{}")) {
            return null;
        } else {
            try {
                Map<String, String> rawSeparatedJSON = separateJSON(string);
                GatewayHelloEvent.Data toReturn = new GatewayHelloEvent.Data(
                    extractInt(rawSeparatedJSON.get("\"heartbeat_interval\""))
                );
                return toReturn;
            } catch (MalformedException malformedException) {
                GeneralFormatter.printException("Couldn't produce a GatewayHelloEvent.Data Object.", malformedException);
                return null;
            }
        }
    }

    public static GatewayData.SessionStartLimit jsonToDiscordGatewayDataSessionStartLimit(String string) {
        if (string == null) {
            return null;
        } else if (string.equals("{}")) {
            return null;
        } else {
            try {
                Map<String, String> rawSeparatedJSON = separateJSON(string);
                GatewayData.SessionStartLimit toReturn = new GatewayData.SessionStartLimit(
                    extractInt(rawSeparatedJSON.get("\"total\"")),
                    extractInt(rawSeparatedJSON.get("\"remaining\"")),
                    extractInt(rawSeparatedJSON.get("\"reset_after\"")),
                    extractInt(rawSeparatedJSON.get("\"max_concurrency\""))
                );
                return toReturn;
            } catch (MalformedException malformedException) {
                GeneralFormatter.printException("Couldn't produce a GatewayData.SessionStartLimit Object.", malformedException);
                return null;
            }
        }
    }

    public static GatewayReadyEventData extractReadyEvent(String string) {
        if (string == null) {
            return null;
        } else if (string.equals("{}")) {
            return null;
        } else {
            try {
                Map<String, String> rawSeparatedJSON = separateJSON(string);
                GatewayReadyEventData toReturn = new GatewayReadyEventData(
                    extractString(rawSeparatedJSON.get("\"session_id\"")),
                    extractString(rawSeparatedJSON.get("\"resume_gateway_url\""))
                );
                return toReturn;
            } catch (MalformedException malformedException) {
                GeneralFormatter.printException("Couldn't produce a GatewayReadyEventData Object.", malformedException);
                return null;
            }
        }
    }

    public static ApplicationCommand.Option jsonToDiscordApplicationCommandOption(String string) {
        if (string == null) {
            return null;
        } else if (string.equals("{}")) {
            return null;
        } else {
            try {
                Map<String, String> rawSeparatedJSON = separateJSON(string);
                ApplicationCommand.Option toReturn = new ApplicationCommand.Option(
                    extractInt(rawSeparatedJSON.get("\"type\"")),
                    extractString(rawSeparatedJSON.get("\"name\"")),
                    extractString(rawSeparatedJSON.get("\"description\""))
                );
                return toReturn;
            } catch (MalformedException malformedException) {
                GeneralFormatter.printException("Couldn't produce a ApplicationCommand.Option Object.", malformedException);
                return null;
            }
        }
    }

    public static User[] jsonArrayToDiscordUsers(String string) {
        if (string == null) {
            return null;
        } else if (string.equals("[]")) {
            return new User[0];
        } else {
            try {
                String[] rawArray = separateArray(string);
                User[] toReturn = new User[rawArray.length];
                for (int i = 0; i < rawArray.length; i++) {
                    toReturn[i] = jsonToDiscordUser(rawArray[i]);
                }
                return toReturn;
            } catch (MalformedException malformedException) {
                GeneralFormatter.printException("Couldn't produce a User Array.", malformedException);
                return null;
            }
        }
    }

    public static Guild[] jsonArrayToDiscordGuilds(String string) {
        if (string == null) {
            return null;
        } else if (string.equals("[]")) {
            return new Guild[0];
        } else {
            try {
                String[] rawArray = separateArray(string);
                Guild[] toReturn = new Guild[rawArray.length];
                for (int i = 0; i < rawArray.length; i++) {
                    toReturn[i] = jsonToDiscordGuild(rawArray[i]);
                }
                return toReturn;
            } catch (MalformedException malformedException) {
                GeneralFormatter.printException("Couldn't produce a Guild Array.", malformedException);
                return null;
            }
        }
    }

    public static Channel[] jsonArrayToDiscordChannels(String string) {
        if (string == null) {
            return null;
        } else if (string.equals("[]")) {
            return new Channel[0];
        } else {
            try {
                String[] rawArray = separateArray(string);
                Channel[] toReturn = new Channel[rawArray.length];
                for (int i = 0; i < rawArray.length; i++) {
                    toReturn[i] = jsonToDiscordChannel(rawArray[i]);
                }
                return toReturn;
            } catch (MalformedException malformedException) {
                GeneralFormatter.printException("Couldn't produce a Channel Array.", malformedException);
                return null;
            }
        }
    }

    public static Message[] jsonArrayToDiscordMessages(String string) {
        if (string == null) {
            return null;
        } else if (string.equals("[]")) {
            return new Message[0];
        } else {
            try {
                String[] rawArray = separateArray(string);
                Message[] toReturn = new Message[rawArray.length];
                for (int i = 0; i < rawArray.length; i++) {
                    toReturn[i] = jsonToDiscordMessage(rawArray[i]);
                }
                return toReturn;
            } catch (MalformedException malformedException) {
                GeneralFormatter.printException("Couldn't produce a Message Array.", malformedException);
                return null;
            }
        }
    }

    public static SlashCommandInteraction.Data.Option[] jsonArrayToDiscordSlashCommandInteractionDataOptions(String string) {
        if (string == null) {
            return null;
        } else if (string.equals("[]")) {
            return new SlashCommandInteraction.Data.Option[0];
        } else {
            try {
                String[] rawArray = separateArray(string);
                SlashCommandInteraction.Data.Option[] toReturn = new SlashCommandInteraction.Data.Option[rawArray.length];
                for (int i = 0; i < rawArray.length; i++) {
                    toReturn[i] = jsonToDiscordSlashCommandInteractionDataOption(rawArray[i]);
                }
                return toReturn;
            } catch (MalformedException malformedException) {
                GeneralFormatter.printException("Couldn't produce a SlashCommandInteraction.Data.Option Array.", malformedException);
                return null;
            }
        }
    }

    public static ModalInteraction.Data.ModalComponent[] jsonArrayToModalInteractionDataModalComponents(String string) {
        if (string == null) {
            return null;
        } else if (string.equals("[]")) {
            return new ModalInteraction.Data.ModalComponent[0];
        } else {
            try {
                String[] rawArray = separateArray(string);
                ModalInteraction.Data.ModalComponent[] toReturn = new ModalInteraction.Data.ModalComponent[rawArray.length];
                for (int i = 0; i < rawArray.length; i++) {
                    toReturn[i] = jsonToDiscordModalInteractionDataModalComponent(rawArray[i]);
                }
                return toReturn;
            } catch (MalformedException malformedException) {
                GeneralFormatter.printException("Couldn't produce a ModalInteraction.Data.ModalComponent Array.", malformedException);
                return null;
            }
        }
    }

    public static ApplicationCommand[] jsonArrayToDiscordApplicationCommands(String string) {
        if (string == null) {
            return null;
        } else if (string.equals("[]")) {
            return new ApplicationCommand[0];
        } else {
            try {
                String[] rawArray = separateArray(string);
                ApplicationCommand[] toReturn = new ApplicationCommand[rawArray.length];
                for (int i = 0; i < rawArray.length; i++) {
                    toReturn[i] = jsonToDiscordApplicationCommand(rawArray[i]);
                }
                return toReturn;
            } catch (MalformedException malformedException) {
                GeneralFormatter.printException("Couldn't produce a ApplicationCommand Array.", malformedException);
                return null;
            }
        }
    }

    public static ApplicationCommand.Option[] jsonArrayToDiscordApplicationCommandOptions(String string) {
        if (string == null) {
            return null;
        } else if (string.equals("null")) {
            return null;
        } else if (string.equals("[]")) {
            return new ApplicationCommand.Option[0];
        } else {
            try {
                String[] rawArray = separateArray(string);
                ApplicationCommand.Option[] toReturn = new ApplicationCommand.Option[rawArray.length];
                for (int i = 0; i < rawArray.length; i++) {
                    toReturn[i] = jsonToDiscordApplicationCommandOption(rawArray[i]);
                }
                return toReturn;
            } catch (MalformedException malformedException) {
                GeneralFormatter.printException("Couldn't produce a ApplicationCommand.Option Array.", malformedException);
                return null;
            }
        }
    }
}
