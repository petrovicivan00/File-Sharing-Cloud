package servent.message;


import app.MyFile;

import java.io.Serial;

public class AddInfoMessage extends BasicMessage {

    @Serial
    private static final long serialVersionUID = 7277911492888707017L;
    private final String requesterIpAddress;
    private final int requesterPort;
    private final MyFile file;

    public AddInfoMessage(String senderIpAddress, int senderPort, String receiverIpAddress, int receiverPort,
                          String requesterIpAddress, int requesterPort, MyFile file) {
        super(MessageType.ADD_INFO, senderIpAddress, senderPort, receiverIpAddress, receiverPort);

        this.requesterIpAddress = requesterIpAddress;
        this.requesterPort = requesterPort;
        this.file = file;
    }

    public String getRequesterIpAddress() { return requesterIpAddress; }

    public int getRequesterPort() { return requesterPort; }

    public MyFile getFile() { return file; }

}
