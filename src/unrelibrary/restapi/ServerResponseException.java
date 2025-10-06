package unrelibrary.restapi;

// this is for when the server returns an error code
public class ServerResponseException extends Exception {
    
    public ServerResponseException(String message) {
        super(message);
    }
}
