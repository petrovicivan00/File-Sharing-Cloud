package servent.message;

public class SorryMessage extends BasicMessage {

	private static final long serialVersionUID = 8866336621366084210L;

	public SorryMessage(String senderIpAddress, int senderPort, String receiverIpAddress, int receiverPort) {
		super(MessageType.SORRY, senderIpAddress, senderPort, receiverIpAddress, receiverPort);
	}
}
