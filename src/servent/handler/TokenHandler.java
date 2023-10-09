package servent.handler;

import app.AppConfig;
import mutex.TokenMutex;
import servent.message.Message;
import servent.message.MessageType;

public class TokenHandler implements MessageHandler {

    private final Message clientMessage;

    public TokenHandler(Message clientMessage) { this.clientMessage = clientMessage; }

    @Override
    public void run() {

        if (clientMessage.getMessageType() == MessageType.TOKEN) {
            TokenMutex.receiveToken();
        } else {
            AppConfig.timestampedErrorPrint("Token handler got message that's not of type TOKEN.");
        }

    }

}
