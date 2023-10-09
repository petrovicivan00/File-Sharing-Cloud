package app;

import app.MyFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class FileUtils {
    private static final String FILE_SEPARATOR = File.separator;

    public static boolean isPathFile(String rootDirectory, String path) {
        File f = new File(constructFullPath(rootDirectory, path));
        System.out.println("# file exists");
        return f.isFile();
    }

    public static MyFile getFileInfoFromPath(String rootDirectory, String path) {
        path = constructFullPath(rootDirectory, path);
        File f = new File(path);

        if (!f.exists() || f.isDirectory()) {
            AppConfig.timestampedErrorPrint("File " + path + " doesn't exist or it's a directory.");
            return null;
        }

        try {
            String filePath = path.replace(rootDirectory + FILE_SEPARATOR, "");
            String fileContent = readFileContent(f);
            return new MyFile(filePath, fileContent, AppConfig.myServentInfo.getChordId());
        } catch (IOException e) {
            AppConfig.timestampedErrorPrint("Couldn't read " + path + ".");
        }

        return null;
    }

    public static List<MyFile> getDirectoryInfoFromPath(String rootDirectory, String path) {
        List<MyFile> files = new ArrayList<>();
        path = constructFullPath(rootDirectory, path);
        File f = new File(path);

        if (!f.exists() || f.isFile()) {
            AppConfig.timestampedErrorPrint("Directory " + path + " doesn't exist or it's a file.");
            return files;
        }

        Queue<String> dirs = new LinkedList<>();
        dirs.add(path);

        while (!dirs.isEmpty()) {
            String dirPath = dirs.poll();
            List<String> subFiles = new ArrayList<>();
            File dir = new File(dirPath);

            for (File file : dir.listFiles()) {
                String filePath = file.getPath().replace(rootDirectory + FILE_SEPARATOR, "");
                subFiles.add(filePath);

                if (file.isFile()) {
                    MyFile myFile = getFileInfoFromPath(rootDirectory, filePath);
                    if (myFile != null) {
                        files.add(myFile);
                    }
                } else {
                    dirs.add(file.getPath());
                }
            }

            dirPath = dirPath.replace(rootDirectory + FILE_SEPARATOR, "");
            files.add(new MyFile(dirPath, subFiles, AppConfig.myServentInfo.getChordId()));
        }

        return files;
    }

    private static String constructFullPath(String rootDirectory, String path) {
        return rootDirectory + FILE_SEPARATOR + path;
    }

    private static String readFileContent(File file) throws IOException {
        StringBuilder fileContent = new StringBuilder();
        BufferedReader reader = new BufferedReader(new FileReader(file));

        String line;
        while ((line = reader.readLine()) != null) {
            fileContent.append(line).append("\n");
        }

        reader.close();
        return fileContent.toString().trim();
    }
}