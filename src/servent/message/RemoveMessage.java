package servent.message;

import java.io.Serial;

public class RemoveMessage extends BasicMessage {

    @Serial
    private static final long serialVersionUID = 2261615800740632985L;

    public RemoveMessage(String senderIpAddress, int senderPort, String receiverIpAddress, int receiverPort, String path) {
        super(MessageType.REMOVE, senderIpAddress, senderPort, receiverIpAddress, receiverPort, path);
    }

}
