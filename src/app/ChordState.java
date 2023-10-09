package app;

import servent.message.Message;
import servent.message.WelcomeMessage;
import servent.message.util.MessageUtil;
import servent.message.AddInfoMessage;
import servent.message.AskPullMessage;
import servent.message.RemoveMessage;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

;


public class ChordState {

    public static int CHORD_SIZE;
    private int chordLevel; //log_2(CHORD_SIZE)

    private ServentInfo[] successorTable;
    private ServentInfo predecessorInfo;

    //we DO NOT use this to send messages, but only to construct the successor table
    private List<ServentInfo> allNodeInfo;

    public List<MyFile> pulledFiles;
    public int amountToPull;
    public int amountPulled;
    private Map<String, MyFile> storageMap;

    public ChordState() {
        this.chordLevel = 1;
        int tmp = CHORD_SIZE;
        while (tmp != 2) {
            if (tmp % 2 != 0) { //not a power of 2
                throw new NumberFormatException();
            }
            tmp /= 2;
            this.chordLevel++;
        }

        successorTable = new ServentInfo[chordLevel];
        for (int i = 0; i < chordLevel; i++) {
            successorTable[i] = null;
        }
        predecessorInfo = null;
        allNodeInfo = new ArrayList<>();
        storageMap = new ConcurrentHashMap<>();
        pulledFiles = new ArrayList<>();
        amountToPull = 0;
        amountPulled = 0;
    }

    public static int chordHash(String value) {

        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = messageDigest.digest(value.getBytes());
            BigInteger hash = new BigInteger(1, bytes).mod(BigInteger.valueOf(64));
            return hash.intValue();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public void addPulledFile(MyFile f) {
        pulledFiles.add(f);
        amountPulled++;
        if (amountPulled == amountToPull) {
            printPulledFiles();
        }
    }


    public void init(WelcomeMessage welcomeMsg) {
        //set a temporary pointer to next node, for sending of update message
        successorTable[0] = new ServentInfo(welcomeMsg.getSenderIpAddress(), welcomeMsg.getSenderPort());
        this.storageMap = new ConcurrentHashMap<>(welcomeMsg.getStorageMap());

        System.out.println("# MY PREDECESSOR IS " + predecessorInfo);
        System.out.println("# MY SUCCESSORS ARE " + Arrays.toString(successorTable));

        try {
            Socket bsSocket = new Socket("localhost", AppConfig.BOOTSTRAP_PORT);

            PrintWriter bsWriter = new PrintWriter(bsSocket.getOutputStream());
            bsWriter.write("New\n" + AppConfig.myServentInfo.getIpAddress() + ":" + AppConfig.myServentInfo.getListenerPort() + "\n");

            bsWriter.flush();
            bsSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
	 * Returns true if we are the owner of the specified key.
	 */
	public boolean isKeyMine(int key) {
		if (predecessorInfo == null) {
			return true;
		}

		int predecessorChordId = predecessorInfo.getChordId();
		int myChordId = AppConfig.myServentInfo.getChordId();

		if (predecessorChordId < myChordId) { //no overflow
			if (key <= myChordId && key > predecessorChordId) {
				return true;
			}
		} else { //overflow
			if (key <= myChordId || key > predecessorChordId) {
				return true;
			}
		}

		return false;
	}


    /**
     * Main chord operation - find the nearest node to hop to to find a specific key.
     * We have to take a value that is smaller than required to make sure we don't overshoot.
     * We can only be certain we have found the required node when it is our first next node.
     */
    public ServentInfo getNextNodeForKey(int key) {
        if (isKeyMine(key)) {
            return AppConfig.myServentInfo;
        }

        //normally we start the search from our first successor
        int startInd = 0;

        //if the key is smaller than us, and we are not the owner,
        //then all nodes up to CHORD_SIZE will never be the owner,
        //so we start the search from the first item in our table after CHORD_SIZE
        //we know that such a node must exist, because otherwise we would own this key
        if (key < AppConfig.myServentInfo.getChordId()) {
            int skip = 1;
            while (successorTable[skip].getChordId() > successorTable[startInd].getChordId()) {
                startInd++;
                skip++;
            }
        }

        int previousId = successorTable[startInd].getChordId();

        for (int i = startInd + 1; i < successorTable.length; i++) {
            if (successorTable[i] == null) {
                AppConfig.timestampedErrorPrint("Couldn't find successor for " + key);
                break;
            }

            int successorId = successorTable[i].getChordId();

            if (successorId >= key) {
                return successorTable[i - 1];
            }
            if (key > previousId && successorId < previousId) { //overflow
                return successorTable[i - 1];
            }
            previousId = successorId;
        }
        //if we have only one node in all slots in the table, we might get here
        //then we can return any item
        return successorTable[0];
    }

    private void updateSuccessorTable() {
        //first node after me has to be successorTable[0]

        int currentNodeIndex = 0;
        ServentInfo currentNode = allNodeInfo.get(currentNodeIndex);
        successorTable[0] = currentNode;

        int currentIncrement = 2;

        ServentInfo previousNode = AppConfig.myServentInfo;

        //i is successorTable index
        for (int i = 1; i < chordLevel; i++, currentIncrement *= 2) {
            //we are looking for the node that has larger chordId than this
            int currentValue = (AppConfig.myServentInfo.getChordId() + currentIncrement) % CHORD_SIZE;

            int currentId = currentNode.getChordId();
            int previousId = previousNode.getChordId();

            //this loop needs to skip all nodes that have smaller chordId than currentValue
            while (true) {
                if (currentValue > currentId) {
                    //before skipping, check for overflow
                    if (currentId > previousId || currentValue < previousId) {
                        //try same value with the next node
                        previousId = currentId;
                        currentNodeIndex = (currentNodeIndex + 1) % allNodeInfo.size();
                        currentNode = allNodeInfo.get(currentNodeIndex);
                        currentId = currentNode.getChordId();
                    } else {
                        successorTable[i] = currentNode;
                        break;
                    }
                } else { //node id is larger
                    ServentInfo nextNode = allNodeInfo.get((currentNodeIndex + 1) % allNodeInfo.size());
                    int nextNodeId = nextNode.getChordId();
                    //check for overflow
                    if (nextNodeId < currentId && currentValue <= nextNodeId) {
                        //try same value with the next node
                        previousId = currentId;
                        currentNodeIndex = (currentNodeIndex + 1) % allNodeInfo.size();
                        currentNode = allNodeInfo.get(currentNodeIndex);
                        currentId = currentNode.getChordId();
                    } else {
                        successorTable[i] = currentNode;
                        break;
                    }
                }
            }
        }
    }

    /**
     * This method constructs an ordered list of all nodes. They are ordered by chordId, starting from this node.
     * Once the list is created, we invoke <code>updateSuccessorTable()</code> to do the rest of the work.
     */
    public void addNodes(List<ServentInfo> newNodes) {
        allNodeInfo.addAll(newNodes);

        allNodeInfo.sort(new Comparator<ServentInfo>() {
            @Override
            public int compare(ServentInfo o1, ServentInfo o2) {
                return o1.getChordId() - o2.getChordId();
            }
        });

        List<ServentInfo> newList = new ArrayList<>();
        List<ServentInfo> newList2 = new ArrayList<>();

        int myId = AppConfig.myServentInfo.getChordId();
        for (ServentInfo serventInfo : allNodeInfo) {
            if (serventInfo.getChordId() < myId) {
                newList2.add(serventInfo);
            } else {
                newList.add(serventInfo);
            }
        }

        allNodeInfo.clear();
        allNodeInfo.addAll(newList);
        allNodeInfo.addAll(newList2);

        if (newList2.size() > 0) {
            predecessorInfo = newList2.get(newList2.size() - 1);
        } else {
            predecessorInfo = newList.get(newList.size() - 1);
        }

        updateSuccessorTable();
    }

    public void addToStorage(MyFile file, String requesterIp, int requesterPort) {
        String filePath = file.getPath();

        // check if the file already exists
        if (storageMap.containsKey(filePath)) {
            AppConfig.timestampedStandardPrint("We already have " + filePath);
            return;
        }

        // add the file to the storage
        storageMap.put(filePath, new MyFile(file));
        AppConfig.timestampedStandardPrint("File " + filePath + " stored successfully.");

        // get the IP and port of the next node
        String nextNodeIp = AppConfig.chordState.getNextNodeIp();
        int nextNodePort = AppConfig.chordState.getNextNodePort();

        // create a new AddInfoMessage and send it
        Message addInfoMsg = new AddInfoMessage(
                AppConfig.myServentInfo.getIpAddress(),
                AppConfig.myServentInfo.getListenerPort(),
                nextNodeIp, nextNodePort,
                requesterIp, requesterPort,
                file
        );
        AppConfig.timestampedStandardPrint("Sending inform message " + addInfoMsg);
        MessageUtil.sendMessage(addInfoMsg);

        // print out the storage map after addition
        System.out.println("# After addition to storage");
        for (Map.Entry<String, MyFile> entry : storageMap.entrySet()) {
            System.out.println("storage = " + entry.getKey() + " -- " + entry.getValue() + " -- " + entry.getValue().getOriginalNode());
        }
    }


    public void pullFile(String path) {
        //Retrieve the file from storage
        List<MyFile> filesToPull = pullFromStorage(path);

        // Verify if path is valid or if any files exist to be pulled
        if (filesToPull == null) {
            AppConfig.timestampedErrorPrint("Invalid path - " + path);
            return;
        } else if (filesToPull.isEmpty()) {
            AppConfig.timestampedErrorPrint("No files found to pull at path - " + path);
            return;
        }

        //Clear the list of pulled files and reset count
        pulledFiles.clear();
        amountPulled = 0;

        // For each file to be pulled, send an AskPullMessage
        for (MyFile fileToPull : filesToPull) {
            Message askMessage = new AskPullMessage(
                    AppConfig.myServentInfo.getIpAddress(),
                    AppConfig.myServentInfo.getListenerPort(),
                    getNextNodeIp(),
                    getNextNodePort(),
                    AppConfig.myServentInfo.getChordId(),
                    fileToPull
            );
            MessageUtil.sendMessage(askMessage);
            AppConfig.timestampedStandardPrint("Sent ask msg " + askMessage);
        }

        amountToPull = filesToPull.size();
    }

    public void printPulledFiles() {
        AppConfig.timestampedStandardPrint("Printing pulled files");
        for (MyFile pulledFile : pulledFiles) {
            System.out.printf("\n----- File Path: %s -----\n", pulledFile.getPath());
            System.out.printf("Content: \n%s\n", pulledFile.getContent());
        }
    }

    public List<MyFile> pullFromStorage(String path) {

        // Early exit if path is not in storageMap
        if (!storageMap.containsKey(path)) {
            return null;
        }

        List<MyFile> filesToReturn = new ArrayList<>();
        MyFile requestedFileInfo = storageMap.get(path);

        if (requestedFileInfo.isDirectory()) {
            // If directory, get all files within directory
            List<String> allDirSubFilePaths = getAllFilesFromDir(requestedFileInfo);

            // Stream API can make the loop more efficient
            allDirSubFilePaths.stream()
                .filter(storageMap::containsKey)
                .map(storageMap::get)
                .forEach(filesToReturn::add);
        } else {
            // If file, simply add it to the list
            filesToReturn.add(requestedFileInfo);
        }

        return filesToReturn;
    }


    private List<String> getAllFilesFromDir(MyFile dir) {
        List<String> filePaths = new ArrayList<>();

        for (String path : dir.getSubFiles()) {
            // Check if path is a file (contains a ".") or a directory
            if (path.contains(".")) {
                filePaths.add(path);
            } else {
                // Recursively get all files from subdirectories
                MyFile subDir = storageMap.get(path);
                if (subDir != null) {
                    filePaths.addAll(getAllFilesFromDir(subDir));
                }
            }
        }
        return filePaths;
    }

    public void removeFileFromStorage(String path) {
        MyFile fileToRemove = storageMap.get(path);

        if (fileToRemove == null) {
            AppConfig.timestampedErrorPrint("File does not exist - " + path);
            return;
        }

        if (!fileToRemove.isDirectory()) {
            removeAndSendRemoveMessage(path);
        } else {
            for (String dirFileToRemove : getAllFilesFromDir(fileToRemove)) {
                removeAndSendRemoveMessage(dirFileToRemove);
            }
        }
    }

    private void removeAndSendRemoveMessage(String path) {
        storageMap.remove(path);
        AppConfig.timestampedStandardPrint("Removing file " + path + " from virtual memory");
        Message removeMessage = new RemoveMessage(AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(),
                getNextNodeIp(), getNextNodePort(), path);
        MessageUtil.sendMessage(removeMessage);
    }


    public ServentInfo[] getSuccessorTable() {
        return successorTable;
    }

    public Map<String, MyFile> getStorageMap() {
        return storageMap;
    }

    public void setStorageMap(Map<String, MyFile> storageMap) {
        this.storageMap = storageMap;
    }

    public int getNextNodePort() {
        return successorTable[0].getListenerPort();
    }

    public String getNextNodeIp() {
        return successorTable[0].getIpAddress();
    }

    public ServentInfo getPredecessor() {
        return predecessorInfo;
    }

    public void setPredecessor(ServentInfo newNodeInfo) {
        this.predecessorInfo = newNodeInfo;
    }

    public List<MyFile> getPulledFiles() {
        return pulledFiles;
    }

    public void setPulledFiles(List<MyFile> pulledFiles) {
        this.pulledFiles = pulledFiles;
    }

    public boolean isCollision(int chordId) {
        if (chordId == AppConfig.myServentInfo.getChordId()) {
            return true;
        }
        for (ServentInfo serventInfo : allNodeInfo) {
            if (serventInfo.getChordId() == chordId) {
                return true;
            }
        }
        return false;
    }
}