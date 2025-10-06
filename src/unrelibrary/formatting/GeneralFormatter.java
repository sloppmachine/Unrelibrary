package unrelibrary.formatting;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import unrelibrary.MalformedException;

// this class provides some basic methods for formatting and working for strings
public class GeneralFormatter {

    protected static String unescapeUnicode(String string) {
        StringBuilder toReturnBuilder = new StringBuilder(string);
        int unicodePosition = toReturnBuilder.indexOf("\\u");
        try {
            while (unicodePosition != -1) {
                int unescaped = Integer.valueOf(toReturnBuilder.substring(unicodePosition+2, unicodePosition+6), 16);
                toReturnBuilder.replace(unicodePosition, unicodePosition + 6, Character.toString((char) unescaped));
                unicodePosition = toReturnBuilder.indexOf("\\u");
            }
        } catch (Exception exception) {
            GeneralFormatter.printException("Couldn't unescape the the unicode sequence " + string, exception);
        }
        return toReturnBuilder.toString();
    }

    // this takes an array of JSONRepresentables, and turns them into the json form (as a string type)
    public static String toJSONArray(JSONRepresentable[] jsonRepresentables) {
        StringBuilder toReturnBuilder = new StringBuilder();
        toReturnBuilder.append("[");
        for (JSONRepresentable jsonRepresentable : jsonRepresentables) {
            if (toReturnBuilder.length() > 1) {
                toReturnBuilder.append(", ");
            }
            toReturnBuilder.append(jsonRepresentable.toJSON());
        }
        toReturnBuilder.append("]");
        return toReturnBuilder.toString();
    }

    protected static String[] separateByCommas(String string) {
        List<String> toReturnBuilder = new LinkedList<String>();
        int roundBracketDepth = 0;
        int squareBracketDepth = 0;
        int curlyBracketDepth = 0;
        int currentIndex = 0;
        boolean inString = false;
        char currentCharacter;
        while (currentIndex < string.length()) {
            currentCharacter = string.charAt(currentIndex);
            switch (currentCharacter) {
                case ',':
                    if (roundBracketDepth == 0 && squareBracketDepth == 0 && curlyBracketDepth == 0 && !inString) {
                        toReturnBuilder.add(string.substring(0, currentIndex));
                        string = string.substring(currentIndex + 1, string.length());
                        currentIndex = 0; // start again from the beginning
                        continue;
                    }
                    break;
                case '(':
                    curlyBracketDepth++;
                    break;
                case ')':
                    curlyBracketDepth--;
                    break;
                case '[':
                    squareBracketDepth++;
                    break;
                case ']':
                    squareBracketDepth--;
                    break;
                case '{':
                    curlyBracketDepth++;
                    break;
                case '}':
                    curlyBracketDepth--;
                    break;
                case '\"':
                    inString = !inString;
                    break;
            }
            currentIndex++;
        }
        toReturnBuilder.add(string);
        for (int i = 0; i < toReturnBuilder.size(); i++) {
            toReturnBuilder.set(i, (toReturnBuilder.get(i).trim()));
        }
        return toReturnBuilder.toArray(new String[toReturnBuilder.size()]);
    }

    public static String[] separateArray(String string) throws MalformedException {
        string = unescapeUnicode(string.trim());
        if (string.charAt(0) != '[' || string.charAt(string.length() - 1) != ']') {
            throw new MalformedException("separateArray received string that doesn't begin and end with []: " + string);
        } else if (string.equals("[]")) {
            return new String[0];
        } else {
            return separateByCommas(string.substring(1, string.length() - 1));
        }
    }

    // this takes a json and transforms it into a map. note that since nothing else changes, the keys look like "\"this\"".
    // public because this is useful af
    public static Map<String, String> separateJSON(String string) throws MalformedException {
        string = unescapeUnicode(string.trim());
        if (string.charAt(0) != '{' || string.charAt(string.length() - 1) != '}') {
            throw new MalformedException("separateJSON received string that doesn't begin and end with {}: " + string);
        } else if (string.equals("{}")) {
            return new TreeMap<String, String>();
        } else {
            Map<String, String> toReturn = new TreeMap<String, String>();
            int splittingIndex;
            for (String entry : separateByCommas(string.substring(1, string.length() - 1))) {
                if ((splittingIndex = entry.indexOf(':')) == -1) {
                    throw new MalformedException("Entry doesn't contain a colon: " + entry);
                } else {
                    toReturn.put(entry.substring(0, splittingIndex).trim(), entry.substring(splittingIndex + 1).trim());
                }
            }
            return toReturn;
        }
    }

    public static void printlnIfVerbose(String toPrint, boolean verbose) {
        if (verbose) {
            System.out.println(toPrint);
        }
        return;
    }

    public static void printException(String comment, Throwable e) {
        System.out.println("ERROR " + comment);
        System.out.println("more information:");
        System.out.println(e);
        e.printStackTrace();
        return;
    }
}
