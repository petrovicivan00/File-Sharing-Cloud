package app;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;


public class AppConfig {

    public static ServentInfo myServentInfo;
    public static int BOOTSTRAP_PORT;
    public static String BOOTSTRAP_ADDRESS;
    public static int SERVENT_COUNT;
    public static String ROOT_DIR;
    public static int WEAK_TIMEOUT;
    public static int STRONG_TIMEOUT;
    public static ChordState chordState;


    public static void readConfig(String configName, int serventId) {
        Properties properties = new Properties();

        try {
            properties.load(new FileInputStream(configName));
        } catch (IOException e) {
            timestampedErrorPrint("Couldn't open properties file. Exiting...");
            System.exit(0);
        }


        BOOTSTRAP_ADDRESS = properties.getProperty("bs_address");
        if (BOOTSTRAP_ADDRESS == null) {
            timestampedErrorPrint("Problem reading bs_address. Exiting...");
            System.exit(0);
        }

        try {
            BOOTSTRAP_PORT = Integer.parseInt(properties.getProperty("bs_port"));
        } catch (NumberFormatException e) {
            timestampedErrorPrint("Problem reading bs_port. Exiting...");
            System.exit(0);
        }

        try {
            SERVENT_COUNT = Integer.parseInt(properties.getProperty("servent_count"));
        } catch (NumberFormatException e) {
            timestampedErrorPrint("Problem reading servent_count. Exiting...");
            System.exit(0);
        }

        ROOT_DIR = properties.getProperty("work_dir" + serventId);
        if (ROOT_DIR == null) {
            System.err.println("Problem reading work_directory property. Exiting...");
            System.exit(0);
        }

        try {
            WEAK_TIMEOUT = Integer.parseInt(properties.getProperty("weak_timeout"));
        } catch (NumberFormatException e) {
            System.err.println("Problem reading weak_timeout property. Exiting...");
            System.exit(0);
        }

        try {
            STRONG_TIMEOUT = Integer.parseInt(properties.getProperty("strong_timeout"));
        } catch (NumberFormatException e) {
            System.err.println("Problem reading strong_timeout property. Exiting...");
            System.exit(0);
        }

        try {
            SERVENT_COUNT = Integer.parseInt(properties.getProperty("servent_count"));
        } catch (NumberFormatException e) {
            timestampedErrorPrint("Problem reading servent_count. Exiting...");
            System.exit(0);
        }

        try {
            int chordSize = Integer.parseInt(properties.getProperty("chord_size"));
            ChordState.CHORD_SIZE = chordSize;
            chordState = new ChordState();
        } catch (NumberFormatException e) {
            timestampedErrorPrint("Problem reading chord_size. Must be a number that is a power of 2. Exiting...");
            System.exit(0);
        }

        String ipAddressProperty = "servent.ip_address";
        String ipAddress = properties.getProperty(ipAddressProperty);
        if (ipAddress == null) {
            timestampedErrorPrint("Problem reading ip_address property. Exiting...");
            System.exit(0);
        }

        String portProperty = "servent" + serventId + ".port";
        int serventPort = -1;
        try {
            serventPort = Integer.parseInt(properties.getProperty(portProperty));
        } catch (NumberFormatException e) {
            timestampedErrorPrint("Problem reading " + portProperty + ". Exiting...");
            System.exit(0);
        }

        myServentInfo = new ServentInfo(ipAddress, serventPort);
    }

    public static void timestampedStandardPrint(String message) {
        DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        Date now = new Date();

        System.out.println(timeFormat.format(now) + " - " + message);
    }

    public static void timestampedErrorPrint(String message) {
        DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        Date now = new Date();

        System.err.println(timeFormat.format(now) + " - " + message);
    }
}