package ploiu.util;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ploiu.model.FolderApproximation;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static ploiu.util.FolderApproximationGenerator.convertDir;

class FolderApproximationGeneratorTests {
    static File root = new File("src/test/resources/FolderApproximationGeneratorTests");

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
    void testConvertFlatDir() throws Exception {
        var dir = createDir("dir");
        var file = createFile("test.txt");
        file.createNewFile();
        dir.mkdir();
        var res = convertDir(root);
        assertEquals(new FolderApproximation(root, List.of(file), List.of(new FolderApproximation(dir, List.of(), List.of()))), res);
    }

    @Test
    void testConvertEmptyDir() {
        var res = convertDir(root);
        assertEquals(new FolderApproximation(root, List.of(), List.of()), res);
    }

    @Test
    void testConvertRecursiveNoFiles() throws IOException {
        var top = createDir("top");
        var middle = createDir("top/middle");
        var bottom = createDir("top/middle/bottom");
        var expected = new FolderApproximation(
                root,
                List.of(),
                List.of(
                        new FolderApproximation(
                                top,
                                List.of(),
                                List.of(
                                        new FolderApproximation(
                                                middle,
                                                List.of(),
                                                List.of(
                                                        new FolderApproximation(
                                                                bottom,
                                                                List.of(),
                                                                List.of()
                                                        )
                                                )
                                        )
                                )
                        )
                )
        );
        var res = convertDir(root);
        assertEquals(expected, res);
    }

    @Test
    void testConvertRecursiveWithFiles() throws IOException {
        var top = createDir("top");
        var rootFile = createFile("root.txt");
        var topFile = createFile("top/top.txt");
        var expected = new FolderApproximation(
                root,
                List.of(rootFile),
                List.of(
                        new FolderApproximation(
                                top,
                                List.of(topFile),
                                List.of()
                        )
                )
        );
        var res = convertDir(root);
        assertEquals(expected, res);
    }

    @Test
    void testConvertRecursivePastLimit() throws IOException {
        // the point of this test is to reject cases with symbolic links, but we can't create symbolic link directories in java sooooo
        var builder = new StringBuilder("0");
        for (int i = 1; i < 51; i++) {
            builder.append("/").append(i);
        }
        createDir(builder.toString());
        var exception = assertThrows(UnsupportedOperationException.class, () -> convertDir(root));
        assertEquals("Possible recursive symlinks: cannot go past depth of 50.", exception.getMessage());
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

    static File createDir(String path) throws IOException {
        var f = new File(root.getPath() + "/" + path);
        f.mkdirs();
        return f;
    }

    static File createFile(String path) throws IOException {
        var f = new File(root.getPath() + "/" + path);
        f.createNewFile();
        return f;
    }
}
