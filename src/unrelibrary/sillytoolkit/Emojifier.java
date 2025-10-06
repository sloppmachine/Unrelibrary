package unrelibrary.sillytoolkit;

import java.util.Random;

// this class can do some basic string editing, inserting discord emojis.
public class Emojifier {
    private static final char[] SEPERATORS = new char[] {'.', ',', '!', '?', '?'}; // discord uses : for emojis so we ignore that
    public static final String[] FUNNY_EMOJIS = new String[] { // public because everybody needs to see this
        ":100:",
        ":cold_face:",
        ":smoking:",
        ":broken_heart:",
        ":muscle:",
        ":flushed:",
        ":sunglasses:",
        ":point_up:",
        ":triumph:",
        ":chart_with_upwards_trend:",
        ":chart_with_downwards_trend:",
        ":face_with_symbols_over_mouth:",
        ":skull:",
        ":wilted_rose:",
        ":goblin:",
    };
    private static int MIN_LENGTH = 6; // minimum and maximum for emoji strings
    private static int MAX_LENGTH = 10; // maximum is exclusive
    
    private static boolean isSeparator(char toCheck) {
        for (char separator : SEPERATORS) {
            if (toCheck == separator) {
                return true;
            }
        }
        return false;
    }

    private static String getRandomEmojis() {
        Random random = new Random();
        int toReturnLength = random.nextInt(MIN_LENGTH, MAX_LENGTH);
        // we don't need to bother with stringbuilders for strings of this size
        String toReturn = "";
        double chanceToSwitch = 0.3;
        String currentEmoji = FUNNY_EMOJIS[random.nextInt(FUNNY_EMOJIS.length)];
        for (int i = 0; i < toReturnLength; i++) {
            toReturn += currentEmoji;
            if (random.nextDouble() <= chanceToSwitch) {
                currentEmoji = FUNNY_EMOJIS[random.nextInt(FUNNY_EMOJIS.length)];
            }
        }
        return toReturn;
    }

    public static String emojify(String message) {
        StringBuilder toReturnBuilder = new StringBuilder(message);
        int currentCharIndex = 0;
        int separatorBegin = -1;
        int separatorEnd = -1;
        char currentChar = ' '; // placeholder
        while (true) {
            if (currentCharIndex >= toReturnBuilder.length()) {
                if (separatorBegin != -1) {
                    toReturnBuilder.replace(separatorBegin, toReturnBuilder.length(), getRandomEmojis());
                }
                break;
            }
            currentChar = toReturnBuilder.charAt(currentCharIndex);
            if (isSeparator(currentChar)) {
                if (separatorBegin == -1) {
                    separatorBegin = currentCharIndex;
                }
            } else {
                if (separatorBegin != -1) {
                    separatorEnd = currentCharIndex;
                    toReturnBuilder.replace(separatorBegin, separatorEnd, getRandomEmojis());
                    separatorBegin = -1;
                    separatorEnd = -1;
                    currentCharIndex = 0;
                }
            }
            currentCharIndex++;
        }
        return toReturnBuilder.toString();
    }
}
