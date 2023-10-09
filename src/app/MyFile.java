package app;

import app.ChordState;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

public class MyFile implements Serializable {

    @Serial
    private static final long serialVersionUID = 463426265374700139L;

    private final String path;
    private final String content;
    private final boolean isDirectory;
    private final List<String> subFiles;
    private final int originalNode;

    public MyFile(String path, boolean isDirectory, String content, List<String> subFiles, int originalNode) {
        this.path = path;
        this.isDirectory = isDirectory;
        this.content = content;
        this.subFiles = List.copyOf(subFiles);
        this.originalNode = originalNode;
    }

    public MyFile(String path, String content, int originalNode) {
        this(path, false, content, List.of(), originalNode);
    }

    public MyFile(String path, List<String> subFiles, int originalNode) {
        this(path, true, "", subFiles, originalNode);
    }

    public MyFile(MyFile myFile) {
        this(myFile.getPath(), myFile.isDirectory(), myFile.getContent(), myFile.getSubFiles(), myFile.getOriginalNode());
    }

    public String getPath() {
        return path;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public int getOriginalNode() {
        return originalNode;
    }

    public String getContent() {
        return content;
    }

    public List<String> getSubFiles() {
        return List.copyOf(subFiles);
    }

    @Override
    public int hashCode() {
        return ChordState.chordHash(path);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MyFile myFile = (MyFile) o;
        return hashCode() == myFile.hashCode();
    }

    @Override
    public String toString() {
        return isDirectory ?
                String.format("[%s {%s}] - inside node: %d", path, getSubFiles(), originalNode) :
                String.format("[%s] - inside node: %d", path, originalNode);
    }

}