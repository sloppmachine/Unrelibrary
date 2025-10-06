package unrelibrary.gatewayapi;

import java.util.function.BiConsumer;

import unrelibrary.APICommunicationManager;
import unrelibrary.discordobjects.Message;

// this class can be used by the user to define what happens when an event arrives
public class EventReceiver {

    public BiConsumer<Message, APICommunicationManager> ON_MESSAGE_CREATE = (input1, input2) -> {};
}
