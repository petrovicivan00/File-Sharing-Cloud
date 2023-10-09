package cli.command;

import app.AppConfig;
import app.MyFile;
import app.FileUtils;
import mutex.TokenMutex;

import java.util.List;

public class AddCommand implements CLICommand {

    @Override
    public String commandName() {
        return "add";
    }

    @Override
    public void execute(String args) {

        if (args == null || args.isEmpty()) {
            AppConfig.timestampedStandardPrint("Invalid argument for add command. Should be add path.");
            return;
        }

        String path = args;

        TokenMutex.lock();

        processFilePath(path);

        TokenMutex.unlock();
    }

    private void processFilePath(String path) {
        if (FileUtils.isPathFile(AppConfig.ROOT_DIR, path)) {
            MyFile file = FileUtils.getFileInfoFromPath(AppConfig.ROOT_DIR, path);
            if (file != null) {
                AppConfig.chordState.addToStorage(file, AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort());
            }
        } else {
            processDirectoryPath(path);
        }
    }

    private void processDirectoryPath(String path) {
        List<MyFile> files = FileUtils.getDirectoryInfoFromPath(AppConfig.ROOT_DIR, path);
        if (!files.isEmpty()) {
            for (MyFile f : files) {
                AppConfig.chordState.addToStorage(f, AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort());
            }
        }
    }
}