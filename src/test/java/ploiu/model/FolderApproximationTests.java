package ploiu.model;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FolderApproximationTests {
    static File root = new File("src/test/resources/FolderApproximationTests");

    @BeforeEach
    void setup() {
        if (root.exists()) {
            deleteDirectory(root);
        }
        root.mkdirs();
    }

    @AfterAll
    static void teardown() {
        deleteDirectory(root);
    }

    @Test
    void testNullInputs() {
        NullPointerException e;
        e = assertThrows(NullPointerException.class, () -> new FolderApproximation(null, List.of(), List.of()));
        assertEquals("Self cannot be null.", e.getMessage());
        e = assertThrows(NullPointerException.class, () -> new FolderApproximation(new File(""), null, List.of()));
        assertEquals("ChildFiles cannot be null.", e.getMessage());
        e = assertThrows(NullPointerException.class, () -> new FolderApproximation(new File(""), List.of(), null));
        assertEquals("ChildFolders cannot be null.", e.getMessage());
    }

    @Test
    void testChildFilesNoDirs() {
        var dir = new File(root.getAbsolutePath() + "/dir");
        dir.mkdir();
        var e = assertThrows(UnsupportedOperationException.class, () -> new FolderApproximation(root, List.of(dir), List.of()));
        assertTrue(e.getMessage().startsWith("Your code is broken: directories are in the list of child files: \n\t"));
    }

    @Test
    void testDetectSymLinks() throws IOException {
        var first = new File(root.getAbsolutePath() + "/base");
        first.createNewFile();
        var link = Files.createSymbolicLink(Path.of(root.getAbsolutePath() + "/link"), first.toPath());
        var e = assertThrows(UnsupportedOperationException.class, () -> new FolderApproximation(root, List.of(first, link.toFile()), List.of()));
        assertEquals("Cannot upload symbolic links. Fix these file paths: \n\t" + link.toAbsolutePath(), e.getMessage());
    }

    // I didn't want to deal with writing this for a test. Thanks baeldung (https://www.baeldung.com/java-delete-directory)
    private static boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }

}
