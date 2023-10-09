package servent.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import app.AppConfig;
import app.ServentInfo;
import app.MyFile;
import mutex.TokenMutex;
import servent.handler.MessageHandler;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.NewNodeMessage;
import servent.message.SorryMessage;
import servent.message.WelcomeMessage;
import servent.message.util.MessageUtil;

public class NewNodeHandler implements MessageHandler {

	private Message clientMessage;

	public NewNodeHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}

	@Override
	public void run() {
		if (clientMessage.getMessageType() == MessageType.NEW_NODE) {
			String newNodeIp = clientMessage.getSenderIpAddress();
			int newNodePort = clientMessage.getSenderPort();
			ServentInfo newNodeInfo = new ServentInfo(newNodeIp, newNodePort);

			//check if the new node collides with another existing node.
			if (AppConfig.chordState.isCollision(newNodeInfo.getChordId())) {
				Message sry = new SorryMessage(AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(),
						clientMessage.getSenderIpAddress(), clientMessage.getSenderPort());
				MessageUtil.sendMessage(sry);
				return;
			}

			//check if he is my predecessor
			boolean isMyPred = AppConfig.chordState.isKeyMine(newNodeInfo.getChordId());
			if (isMyPred) { //if yes, prepare and send welcome message

				//Pozivamo mutex lock
				TokenMutex.lock();//LOCK 1n

				ServentInfo hisPred = AppConfig.chordState.getPredecessor();
				if (hisPred == null) {
					hisPred = AppConfig.myServentInfo;
				}

				AppConfig.chordState.setPredecessor(newNodeInfo);

				Map<String, MyFile> myStorage = AppConfig.chordState.getStorageMap();

//				int myId = AppConfig.myServentInfo.getChordId();
//				int hisPredId = hisPred.getChordId();
//				int newNodeId = newNodeInfo.getChordId();
//				for (Entry<Integer, FileInfo> fileInfoEntry : myStorage.entrySet()) {
//					if (hisPredId == myId) { //i am first and he is second
//						if (myId < newNodeId) {
//							if (fileInfoEntry.getKey() <= newNodeId && fileInfoEntry.getKey() > myId) {
//								hisStorage.put(fileInfoEntry.getKey(), fileInfoEntry.getValue());
//							}
//						}
//						else {
//							if (fileInfoEntry.getKey() <= newNodeId || fileInfoEntry.getKey() > myId) {
//								hisStorage.put(fileInfoEntry.getKey(), fileInfoEntry.getValue());
//							}
//						}
//					}
//					if (hisPredId < myId) { //my old predecesor was before me
//						if (fileInfoEntry.getKey() <= newNodeId) {
//							hisStorage.put(fileInfoEntry.getKey(), fileInfoEntry.getValue());
//						}
//					}
//					else { //my old predecesor was after me
//						if (hisPredId > newNodeId) { //new node overflow
//							if (fileInfoEntry.getKey() <= newNodeId || fileInfoEntry.getKey() > hisPredId) {
//								hisStorage.put(fileInfoEntry.getKey(), fileInfoEntry.getValue());
//							}
//						} else { //no new node overflow
//							if (fileInfoEntry.getKey() <= newNodeId && fileInfoEntry.getKey() > hisPredId) {
//								hisStorage.put(fileInfoEntry.getKey(), fileInfoEntry.getValue());
//							}
//						}
//
//					}
//				}
//				for (Integer key : hisStorage.keySet()) { //remove his values from my map
//					myStorage.remove(key);
//				}
//				AppConfig.chordState.setStorageMap(myStorage);

				Map<String, MyFile> hisStorage = new HashMap<>(myStorage);

				WelcomeMessage wm = new WelcomeMessage(AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(),
						newNodeIp, newNodePort, hisStorage);
				MessageUtil.sendMessage(wm);
			}
			else { //if he is not my predecessor, let someone else take care of it
				ServentInfo nextNode = AppConfig.chordState.getNextNodeForKey(newNodeInfo.getChordId());
				NewNodeMessage nnm = new NewNodeMessage(newNodeIp, newNodePort, nextNode.getIpAddress(), nextNode.getListenerPort());
				MessageUtil.sendMessage(nnm);
			}

		}
		else {
			AppConfig.timestampedErrorPrint("NEW_NODE handler got something that is not new node message.");
		}
	}
}