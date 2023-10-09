package servent.message;

import java.io.Serial;

public class JoinedMessage extends BasicMessage {

    @Serial
    private static final long serialVersionUID = -4381135751918243813L;

    public JoinedMessage(String senderIpAddress, int senderPort, String receiverIpAddress, int receiverPort) {
        super(MessageType.JOINED, senderIpAddress, senderPort, receiverIpAddress, receiverPort);
    }

}
