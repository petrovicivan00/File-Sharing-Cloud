package servent.message;

import app.MyFile;

import java.util.Map;

public class WelcomeMessage extends BasicMessage {

	private static final long serialVersionUID = -8981406250652693908L;

	private final Map<String, MyFile> storageMap;
	
	public WelcomeMessage(String senderIpAddress, int senderPort, String receiverIpAddress, int receiverPort, Map<String, MyFile> storageMap) {
		super(MessageType.WELCOME, senderIpAddress, senderPort, receiverIpAddress, receiverPort);
		this.storageMap = storageMap;
	}

	public Map<String, MyFile> getStorageMap() {
		return storageMap;
	}

}
