package servent.message;

import java.io.Serial;

public class TokenMessage extends BasicMessage {

    @Serial
    private static final long serialVersionUID = -4345742380547149474L;

    public TokenMessage(String senderIpAddress, int senderPort, String receiverIpAddress, int receiverPort) {
        super(MessageType.TOKEN, senderIpAddress, senderPort, receiverIpAddress, receiverPort);
    }

}
