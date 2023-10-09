package mutex;

import app.AppConfig;
import servent.message.Message;
import servent.message.TokenMessage;
import servent.message.util.MessageUtil;

import java.util.concurrent.atomic.AtomicBoolean;

public class TokenMutex {

    private static final AtomicBoolean localLock = new AtomicBoolean(false);

    private static volatile boolean haveToken = false;
    private static volatile boolean wantLock = false;

    public static void init() {
        haveToken = true;
    }

    public static void lock() {
        wantLock = true;

        long sleepTime = 1;
        while (!haveToken) {
            try {
                Thread.sleep(sleepTime);
                sleepTime = (sleepTime * 2) > 100 ? 100 : (sleepTime * 2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void unlock() {
        haveToken = false;
        wantLock = false;
        sendToken();
    }

    public static void receiveToken() {
        if (wantLock) {
            haveToken = true;
        } else {
            sendToken();
        }
    }

    private static void sendToken() {
        String nextNodeIp = AppConfig.chordState.getNextNodeIp();
        int nextNodePort = AppConfig.chordState.getNextNodePort();
        Message tokenMessage = new TokenMessage(AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(), nextNodeIp, nextNodePort);
        MessageUtil.sendMessage(tokenMessage);
    }
}