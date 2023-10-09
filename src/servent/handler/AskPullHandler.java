package servent.handler;

import app.AppConfig;
import app.MyFile;
import servent.message.AskPullMessage;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.TellPullMessage;
import servent.message.util.MessageUtil;


public class AskPullHandler implements MessageHandler {

	private final Message clientMessage;
	
	public AskPullHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}
	
	@Override
	public void run() {
		if (clientMessage.getMessageType() == MessageType.ASK_PULL) {
			AskPullMessage askPullMessage = (AskPullMessage) clientMessage;

			if (askPullMessage.getFile().getOriginalNode() == AppConfig.myServentInfo.getChordId()){//ako smo mi od koga treba da povucemo
				MyFile fileToSendBack = AppConfig.chordState.getStorageMap().get(askPullMessage.getFile().getPath());
				if (fileToSendBack != null) {
					Message tellMessage = new TellPullMessage(AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(),
							AppConfig.chordState.getNextNodeIp(), AppConfig.chordState.getNextNodePort(),
							askPullMessage.getSenderIpAddress(), askPullMessage.getRequesterId(), fileToSendBack);
					MessageUtil.sendMessage(tellMessage);
				}
			}
			else {
				Message askMessage = new AskPullMessage(AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(),
						AppConfig.chordState.getNextNodeIp(), AppConfig.chordState.getNextNodePort(),askPullMessage.getRequesterId(), askPullMessage.getFile());
				MessageUtil.sendMessage(askMessage);
			}

		} else {
			AppConfig.timestampedErrorPrint("Ask get handler got a message that is not ASK_GET");
		}
	}

}