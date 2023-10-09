package servent.message;

import java.io.Serial;

public class UpdateMessage extends BasicMessage {

	@Serial
	private static final long serialVersionUID = 3586102505319194978L;

	public UpdateMessage(String senderIpAddress, int senderPort, String receiverIpAddress, int receiverPort, String text) {
		super(MessageType.UPDATE, senderIpAddress, senderPort, receiverIpAddress, receiverPort, text);
	}
}
