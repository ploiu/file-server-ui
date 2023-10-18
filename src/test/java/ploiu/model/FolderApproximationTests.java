package ploiu.model;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ploiu.TestHelper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FolderApproximationTests {
    static File root = new File("src/test/resources/FolderApproximationTests");
    static TestHelper helper = new TestHelper(root);

    @BeforeEach
    void setup() {
        if (root.exists()) {
        }
        root.mkdirs();
    }

    @AfterAll
    static void teardown() {
        helper.deleteDirectory(root);
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

    @Test
    void testSize() throws IOException {
        var top = helper.createDir("top");
        var middle = helper.createDir("top/middle");
        var bottom = helper.createDir("top/middle/bottom");
        var middleFile = helper.createFile("top/middle/test");

        var approximation = new FolderApproximation(root, List.of(), List.of(
                new FolderApproximation(top, List.of(), List.of(
                        new FolderApproximation(middle, List.of(middleFile), List.of(
                                new FolderApproximation(bottom, List.of(), List.of())
                        ))
                ))
        ));
        assertEquals(5, approximation.size());
    }
}
