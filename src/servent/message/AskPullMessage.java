package servent.message;



import app.MyFile;

import java.io.Serial;

public class AskPullMessage extends BasicMessage {

	@Serial
	private static final long serialVersionUID = -8558031124520315033L;
	private final MyFile file;
	private final int requesterId;


	public AskPullMessage(String senderIpAddress, int senderPort,String receiverIpAddress, int receiverPort, int requesterId, MyFile file) {
		super(MessageType.ASK_PULL, senderIpAddress, senderPort, receiverIpAddress, receiverPort);
		this.file = file;
		this.requesterId = requesterId;
	}

	public MyFile getFile() {
		return file;
	}

	public int getRequesterId() {
		return requesterId;
	}


}
