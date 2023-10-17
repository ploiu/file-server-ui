package ploiu.util;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ploiu.TestHelper;
import ploiu.model.FolderApproximation;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static ploiu.util.FolderApproximationGenerator.convertDir;

class FolderApproximationGeneratorTests {
    static File root = new File("src/test/resources/FolderApproximationGeneratorTests");
    static TestHelper helper = new TestHelper(root);

    @BeforeEach
    void setup() {
        if (root.exists()) {
            helper.deleteDirectory(root);
        }
        root.mkdirs();
    }

    @AfterAll
    static void teardown() {
        helper.deleteDirectory(root);
    }

    @Test
    void testConvertFlatDir() throws Exception {
        var dir = helper.createDir("dir");
        var file = helper.createFile("test.txt");
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
        var top = helper.createDir("top");
        var middle = helper.createDir("top/middle");
        var bottom = helper.createDir("top/middle/bottom");
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
        var top = helper.createDir("top");
        var rootFile = helper.createFile("root.txt");
        var topFile = helper.createFile("top/top.txt");
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
        helper.createDir(builder.toString());
        var exception = assertThrows(UnsupportedOperationException.class, () -> convertDir(root));
        assertEquals("Possible recursive symlinks: cannot go past depth of 50.", exception.getMessage());
    }
}
