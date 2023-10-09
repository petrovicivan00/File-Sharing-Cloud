package app;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BootstrapServer {

    private final List<String> activeServents;
    private final ExecutorService threadPool = Executors.newWorkStealingPool();
    private final Random rand = new Random(System.currentTimeMillis());
    private volatile boolean working = true;


    private class CLIWorker implements Runnable {
        @Override
        public void run() {
            Scanner sc = new Scanner(System.in);

            String line;
            while (true) {
                line = sc.nextLine();

                if (line.equals("stop")) {
                    working = false;
                    break;
                }
            }

            sc.close();
        }
    }
    
    public BootstrapServer() {
        activeServents = new CopyOnWriteArrayList<>();
    }

    public void doBootstrap(int bsPort) {

        Thread cliThread = new Thread(new CLIWorker());
        cliThread.start();

        ServerSocket listenerSocket = null;
        try {
            listenerSocket = new ServerSocket(bsPort);
            listenerSocket.setSoTimeout(1000);
        } catch (IOException e1) {
            AppConfig.timestampedErrorPrint("Problem while opening listener socket.");
            System.exit(0);
        }

        while (working) {
            try {
                Socket newServentSocket = listenerSocket.accept();
                Scanner socketScanner = new Scanner(newServentSocket.getInputStream());
                String message = socketScanner.nextLine();

                threadPool.submit(new MessageHandler(message, newServentSocket, socketScanner));
            } catch (SocketTimeoutException e) {
                //e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private class MessageHandler implements Runnable {

        private final String message;
        private final Socket newServentSocket;
        private final Scanner socketScanner;

        public MessageHandler(String message, Socket newServentSocket, Scanner socketScanner) {

            this.message = message;
            this.newServentSocket = newServentSocket;
            this.socketScanner = socketScanner;

        }

        @Override
        public void run() {

            switch (message) {
                case "Hail" -> {
                    String newIpPort = socketScanner.nextLine();
                    System.out.println("got " + newIpPort);

                    try {
                        PrintWriter socketWriter = new PrintWriter(newServentSocket.getOutputStream());

                        if (activeServents.size() == 0) {
                            System.out.println("adding " + newIpPort);
                            socketWriter.write("-1\n");
                            activeServents.add(newIpPort);
                        } else {
                            String randServent = activeServents.get(rand.nextInt(activeServents.size()));
                            socketWriter.write(randServent + "\n");
                        }

                        socketWriter.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                case "New" -> {
                    String newIpPort = socketScanner.nextLine();
                    System.out.println("adding " + newIpPort);
                    activeServents.add(newIpPort);
                }
                case "Bye" -> {
                    String newIpPort = socketScanner.nextLine();
                    System.out.println("removing " + newIpPort);
                    activeServents.remove(newIpPort);
                }
            }

            try {
                newServentSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    /**
     * Expects one command line argument - the port to listen on.
     */
    public static void main(String[] args) {

        if (args.length != 1) {
            AppConfig.timestampedErrorPrint("Bootstrap started without port argument.");
            return;
        }

        int bsPort = 0;
        try {
            bsPort = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            AppConfig.timestampedErrorPrint("Bootstrap port not valid: " + args[0]);
            System.exit(0);
        }

        AppConfig.timestampedStandardPrint("Bootstrap server started on port: " + bsPort);

        BootstrapServer bs = new BootstrapServer();
        bs.doBootstrap(bsPort);

    }
}
