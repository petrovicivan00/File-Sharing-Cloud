package servent.handler;

import app.AppConfig;
import servent.message.Message;
import servent.message.MessageType;

public class RemoveHandler implements MessageHandler {

    private final Message clientMessage;

    public RemoveHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {

        if (clientMessage.getMessageType() == MessageType.REMOVE) {
            AppConfig.chordState.removeFileFromStorage(clientMessage.getMessageText());
        }
        else {
            AppConfig.timestampedErrorPrint("Remove handler got message that's not of type REMOVE.");
        }

    }

}
