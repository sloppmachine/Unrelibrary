package unrelibrary;
// this is for string formatting, like when we expect a string that contains an array, but get a sequence of symbols that doesn't start and end with []
public class MalformedException extends Exception {
    
    public MalformedException(String message) {
        super(message);
    }
}