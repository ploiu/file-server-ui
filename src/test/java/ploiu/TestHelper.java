package ploiu;

import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.IOException;

@RequiredArgsConstructor
public class TestHelper {
    private final File root;

    // I didn't want to deal with writing this for a test. Thanks baeldung (https://www.baeldung.com/java-delete-directory)
    public boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }

    public File createDir(String path) throws IOException {
        var f = new File(root.getPath() + "/" + path);
        f.mkdirs();
        return f;
    }

    public File createFile(String path) throws IOException {
        var f = new File(root.getPath() + "/" + path);
        f.createNewFile();
        return f;
    }
}
