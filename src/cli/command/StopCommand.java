package cli.command;

import app.AppConfig;
import cli.CLIParser;
import servent.SimpleServentListener;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class StopCommand implements CLICommand {

	private CLIParser parser;
	private SimpleServentListener listener;

	public StopCommand(CLIParser parser, SimpleServentListener listener) {
		this.parser = parser;
		this.listener = listener;
	}

	@Override
	public String commandName() {
		return "stop";
	}

	@Override
	public void execute(String args) {
		AppConfig.timestampedStandardPrint("Stopping...");

		parser.stop();
		listener.stop();

		String bsAddress = AppConfig.BOOTSTRAP_ADDRESS;
		int bsPort = AppConfig.BOOTSTRAP_PORT;

		try {
			Socket bsSocket = new Socket(bsAddress, bsPort);

			PrintWriter bsWriter = new PrintWriter(bsSocket.getOutputStream());
			bsWriter.write("Bye\n" + AppConfig.myServentInfo.getIpAddress() + ":" + AppConfig.myServentInfo.getListenerPort() + "\n");
			bsWriter.flush();

			bsSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
