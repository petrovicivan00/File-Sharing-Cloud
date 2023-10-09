package servent.handler;

import app.AppConfig;
import app.MyFile;
import app.ChordState;
import app.ServentInfo;
import servent.message.AddInfoMessage;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.util.MessageUtil;

public class AddInfoHandler implements MessageHandler {

    private final Message clientMessage;

    public AddInfoHandler(Message clientMessage) { this.clientMessage = clientMessage; }

    @Override
    public void run() {
        if (clientMessage.getMessageType() == MessageType.ADD_INFO) {
            AddInfoMessage additionInfoMsg = (AddInfoMessage) clientMessage;

            String requesterNode = additionInfoMsg.getReceiverIpAddress() + ":" + additionInfoMsg.getReceiverPort();
            int key = ChordState.chordHash(requesterNode);

            if (key == AppConfig.myServentInfo.getChordId()) {
                MyFile file = additionInfoMsg.getFile();
                AppConfig.chordState.addToStorage(file, additionInfoMsg.getSenderIpAddress(), additionInfoMsg.getSenderPort());
            }
            else {
                ServentInfo nextNode = AppConfig.chordState.getNextNodeForKey(key);
                Message nextSuccessMessage = new AddInfoMessage(
                        additionInfoMsg.getSenderIpAddress(), additionInfoMsg.getSenderPort(),
                        nextNode.getIpAddress(), nextNode.getListenerPort(),
                        additionInfoMsg.getRequesterIpAddress(), additionInfoMsg.getRequesterPort(),
                        additionInfoMsg.getFile());
                MessageUtil.sendMessage(nextSuccessMessage);
            }
        } else {
            AppConfig.timestampedErrorPrint("Add success handler got message that's not of type ADD_SUCCESS.");
        }

    }

}
