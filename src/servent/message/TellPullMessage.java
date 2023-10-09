package servent.message;



import app.MyFile;

import java.io.Serial;

public class TellPullMessage extends BasicMessage {

	@Serial
	private static final long serialVersionUID = -6213394344524749872L;

	private final String requesterIpAddress;
	private final int requesterId;
	private final MyFile file;

	public TellPullMessage(String senderIpAddress, int senderPort, String receiverIpAddress, int receiverPort,
						   String requesterIpAddress, int requesterId, MyFile file) {

		super(MessageType.TELL_PULL, senderIpAddress, senderPort, receiverIpAddress, receiverPort);

		this.requesterIpAddress = requesterIpAddress;
		this.requesterId = requesterId;
		this.file = file;

	}

	public String getRequesterIpAddress() {
		return requesterIpAddress;
	}

	public int getRequesterId() {
		return requesterId;
	}

	public MyFile getFile() {
		return file;
	}

}
