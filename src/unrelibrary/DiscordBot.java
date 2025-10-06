package unrelibrary;

import java.io.FileNotFoundException;
import java.util.Scanner;

import unrelibrary.formatting.GeneralFormatter;
import unrelibrary.gatewayapi.EventReceiver;
import unrelibrary.restapi.ServerResponseException;
import unrelibrary.restapi.SlashCommand;

// high level class for quickly setting up bot behaviour
public class DiscordBot {
    // how about this: initiation values, private values, public values? add this to readme maybe.
    private final boolean VERBOSE;
    private final boolean ONLY_REST; // whether to only use rest api, as to help conserve the daily gateway limits
    private final int SEPERATOR_WIDTH;
    private final boolean VERIFY_ENDPOINT;
    private final String CONFIDENTIAL_LOCATION;

    private boolean initialized = false;
    private boolean online = false;
    private ManualControl manualControl;

    public APICommunicationManager apiCommunicationManager;

    public DiscordBot(boolean onlyRest, int seperatorWidth, boolean verifyEndpoint, String confidentialLocation, boolean verbose) {
        this.VERBOSE = verbose;
        this.ONLY_REST = onlyRest;
        this.SEPERATOR_WIDTH = seperatorWidth;
        this.VERIFY_ENDPOINT = verifyEndpoint;
        this.CONFIDENTIAL_LOCATION = confidentialLocation;
    }

    // initialization will make the bot behaviour ready to be configured
    public boolean initialize(EventReceiver eventReceiver) {
        GeneralFormatter.printlnIfVerbose("Extracting confidental data...", VERBOSE);
        ConfidentialReader confidentialReader;
        try {
            confidentialReader = new ConfidentialReader(CONFIDENTIAL_LOCATION);
        } catch (FileNotFoundException fileNotFoundException) {
            GeneralFormatter.printException("couldn't initalize because no confidential file", fileNotFoundException);
            return false;
        } catch (MalformedException malformedException) {
            GeneralFormatter.printException("couldn't initalize because malformed json in confidential file", malformedException);
            return false;
        }
        apiCommunicationManager = new APICommunicationManager(
            confidentialReader.PORT,
            10, // this is not read from the json. see readme
            confidentialReader.TOKEN,
            confidentialReader.PUBLIC_KEY,
            confidentialReader.APPLICATION_ID,
            this,
            VERBOSE, // this is the only other value not read from a file, so that it can instead be easily controlled by code.
            eventReceiver
        );
        initialized = true;
        return true;
    }

    public void gatewayConnected() {
        manualControl.gatewayConnected();
        return;
    }

    public boolean control() {
        if (online) {
            manualControl.control();
            return true;
        } else {
            GeneralFormatter.printlnIfVerbose("Need to be online to control!", VERBOSE);
            return false;
        }
    }

    // this is used to locally declare a slash command, as well as the local function it calls
    // unrelibrary will automatically notify discord of the currently available slash commands
    public boolean registerSlashCommand(String name, SlashCommand slashCommand) {
        if (!initialized) {
            GeneralFormatter.printlnIfVerbose("You need to initialize first!", VERBOSE);
            return false;
        } else if (online) {
            GeneralFormatter.printlnIfVerbose("Can't edit slash commands while online!", VERBOSE);
            return false;
        } else {
            return apiCommunicationManager.registerSlashCommand(name, slashCommand);
        }
    }

    public boolean deleteSlashCommand(String name) {
        if (!initialized) {
            GeneralFormatter.printlnIfVerbose("You need to initialize first!", VERBOSE);
            return false;
        } else if (online) {
            GeneralFormatter.printlnIfVerbose("Can't edit slash commands while online!", VERBOSE);
            return false;
        } else {
            return apiCommunicationManager.deleteSlashCommand(name);
        }
    }

    public boolean goOnline() { // return true if everything worked, false otherwise
        //Security.addProvider(new BouncyCastleProvider()); // idk if this is necessary
        if (!initialized) {
            GeneralFormatter.printlnIfVerbose("You need to initialize first!", VERBOSE);
            return false;
        } else {
            if (!apiCommunicationManager.setUpREST()) {
                GeneralFormatter.printlnIfVerbose("Aborted connection", VERBOSE);
                return false;
            }
            if (VERIFY_ENDPOINT) {
                Scanner scanner = new Scanner(System.in);
                GeneralFormatter.printlnIfVerbose("waiting for your confirmation. confirming endpoint. confirm this in the developer portal. press enter to continue", VERBOSE);
                scanner.nextLine();
            }
            if (!ONLY_REST) {
                if (!apiCommunicationManager.setUpGateway()) {
                    GeneralFormatter.printlnIfVerbose("Aborted connection", VERBOSE);
                    return false;
                }
            }
            GeneralFormatter.printlnIfVerbose("Connected to discord!", VERBOSE);
            GeneralFormatter.printlnIfVerbose("Setting up manual control...", VERBOSE);
            try {
                manualControl = new ManualControl(SEPERATOR_WIDTH, apiCommunicationManager);
            } catch (ServerResponseException serverResponseException) {
                GeneralFormatter.printException("couldn't get information about self!", serverResponseException);
                return false;
            }

            online = true;
            return true;
        }
    }

    public boolean goOffline() {
        if (!initialized) {
            GeneralFormatter.printlnIfVerbose("You need to initialize first!", VERBOSE);
            return false;
        } else {
            if (online) {
                GeneralFormatter.printlnIfVerbose("Shutting down manual control...", VERBOSE);
                manualControl = null;
                if (!apiCommunicationManager.disconnectREST()) {
                    return false;
                }
                if (!ONLY_REST) {
                    if (!apiCommunicationManager.disconnectGateway()) {
                        return false;
                    }
                }
                return true;
            } else {
                GeneralFormatter.printlnIfVerbose("Can't shut down the bot because it was never initialized!", VERBOSE);
                return false;
            }
        }
    }
}
