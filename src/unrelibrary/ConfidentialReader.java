package unrelibrary;
import java.util.Map;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import unrelibrary.formatting.GeneralFormatter;

// this is for getting the confidential information of the bot (the security stuff)
public class ConfidentialReader extends GeneralFormatter {
    public final int PORT;
    public final String TOKEN;
    public final String PUBLIC_KEY;
    public final long APPLICATION_ID;

    public ConfidentialReader(String confidentialLocation) throws FileNotFoundException, MalformedException {
        File dataFile = new File("./data/confidential.json");
        Scanner scanner = new Scanner(dataFile);
        StringBuilder fileContents = new StringBuilder();
        while (scanner.hasNextLine()) {
            fileContents.append(scanner.nextLine());
        }
        Map<String, String> separatedJSON = separateJSON(fileContents.toString());
        String rawPort = separatedJSON.get("\"port\"");
        PORT = Integer.valueOf(rawPort);
        String rawToken = separatedJSON.get("\"token\"");
        TOKEN = rawToken.substring(1, rawToken.length() - 1);
        String rawPublicKey = separatedJSON.get("\"publicKey\"");
        PUBLIC_KEY = rawPublicKey.substring(1, rawPublicKey.length() - 1);
        String rawApplicationID = separatedJSON.get("\"applicationID\"");
        APPLICATION_ID = Long.valueOf(rawApplicationID);
        scanner.close();
    }
}
