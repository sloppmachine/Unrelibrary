package unrelibrary.formatting;

import java.util.LinkedList;
import java.util.List;

// this class can build a json object as a string from a set of given properties
public class JSONBuilder {
    private List<String> currentState = new LinkedList<String>(); // key-value-pairs are stored in a list. therefore the order of insertion matters

    public void reset() {
        currentState = new LinkedList<String>();
        return;
    }

    public void addNullProperty(String name) {
        currentState.add("\"" + name + "\" : null");
        return;
    }

    public void addBooleanProperty(String name, Boolean property) {
        if (property == null) {
            addNullProperty(name);
        } else if (property) {
            currentState.add("\"" + name + "\" : true");
        } else {
            currentState.add("\"" + name + "\" : false");
        }
        return;
    }

    public void addIntProperty(String name, Integer property) {
        if (property == null) {
            addNullProperty(name);
        } else {
            currentState.add("\"" + name + "\" : " + property.toString());
        }
        return;
    }

    public void addLongProperty(String name, Long property) { // snowflakes need to be wrapped in parentheses
        if (property == null) {
            addNullProperty(name);
        } else {
            currentState.add("\"" + name + "\" : " + Long.valueOf(property).toString());
        }
        return;
    }

    public void addStringProperty(String name, String property) {
        if (property == null) {
            addNullProperty(name);
        } else {
            currentState.add("\"" + name + "\" : \"" + property + "\"");
        }
        return;
    }

    public void addIntArrayProperty(String name, int[] property) {
        if (property == null) {
            addNullProperty(name);
        } else {
            StringBuilder arrayBuilder = new StringBuilder();
            arrayBuilder.append("[");
            for (int context : property) {
                if (arrayBuilder.length() > 1) {
                    arrayBuilder.append(", ");
                }
                arrayBuilder.append(Integer.valueOf(context).toString());
            }
            arrayBuilder.append("]");
            currentState.add("\"contexts\" : " + arrayBuilder.toString());
        }
        return;
    }

    // the specified string value is added to the json without any changes.
    public void addLiteralProperty(String name, String property) {
        if (property == null) {
            addNullProperty(name);
        } else {
            currentState.add("\"" + name + "\" : " + property);
        }
        return;
    }

    public String build() {
        StringBuilder toReturnBuilder = new StringBuilder();
        toReturnBuilder.append("{");
        for (String entry : currentState) {
            if (toReturnBuilder.length() > 1) {
                toReturnBuilder.append(", ");
            }
            toReturnBuilder.append(entry);
        }
        toReturnBuilder.append("}");
        return toReturnBuilder.toString();
    }

    // takes an array of JSONRepresentables, and returns them all in their string form with brackets and commas.
    public static String buildArray(JSONRepresentable[] jsonRepresentables) {
        if (jsonRepresentables == null) {
            return null;
        } else if (jsonRepresentables.length == 0) {
            return "[]";
        } else {
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
    }
}
