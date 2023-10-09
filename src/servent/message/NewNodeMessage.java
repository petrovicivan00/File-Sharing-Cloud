package servent.message;

import java.io.Serial;

public class NewNodeMessage extends BasicMessage {

	@Serial
	private static final long serialVersionUID = -7578851736688155676L;

	public NewNodeMessage(String senderIpAddress, int senderPort, String receiverIpAddress, int receiverPort) {
		super(MessageType.NEW_NODE, senderIpAddress, senderPort, receiverIpAddress, receiverPort);
	}

}
