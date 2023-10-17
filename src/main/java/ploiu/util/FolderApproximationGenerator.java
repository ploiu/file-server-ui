package ploiu.util;

import org.jetbrains.annotations.NotNull;
import ploiu.model.FolderApproximation;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

/**
 * Used to handle transforming File system files and directories into a "middle ground" between the file system and the api.
 * This allows us an easier way to upload entire directories to the server
 */
public class FolderApproximationGenerator {

    public static FolderApproximation convertDir(@NotNull File root) {
        if (!root.isDirectory()) {
            throw new UnsupportedOperationException("Can only read directories.");
        }
        var childFiles = Objects.requireNonNull(root.listFiles(File::isFile), "childFiles is null!");
        var childFolders = Objects.requireNonNull(root.listFiles(f -> f.isDirectory() && !Files.isSymbolicLink(f.toPath().toAbsolutePath())), "childFolders is null!");
        var childApproximations = new ArrayList<FolderApproximation>();
        for (File childFolder : childFolders) {
            childApproximations.add(convertDir(childFolder, 1));
        }
        return new FolderApproximation(root, new ArrayList<>(Arrays.asList(childFiles)), childApproximations);
    }

    static FolderApproximation convertDir(@NotNull File root, int currentDepth) {
        if (currentDepth > 50) {
            throw new UnsupportedOperationException("Possible recursive symlinks: cannot go past depth of 50.");
        }
        if (!root.isDirectory()) {
            throw new UnsupportedOperationException("Can only read directories.");
        }
        var childFiles = Objects.requireNonNull(root.listFiles(File::isFile), "childFiles is null!");
        var childFolders = Objects.requireNonNull(root.listFiles(f -> f.isDirectory() && !Files.isSymbolicLink(f.toPath().toAbsolutePath())), "childFolders is null!");
        var childApproximations = new ArrayList<FolderApproximation>();
        for (File childFolder : childFolders) {
            childApproximations.add(convertDir(childFolder, currentDepth + 1));
        }
        return new FolderApproximation(root, new ArrayList<>(Arrays.asList(childFiles)), childApproximations);
    }


}
