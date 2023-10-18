package ploiu.model;

import lombok.Data;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * represents a "middle ground" between a file system {@link File} and a {@link FolderApi}
 */
@Data
@Accessors(fluent = true)
public final class FolderApproximation {
    private final File self;
    private final Collection<File> childFiles;
    private final Collection<FolderApproximation> childFolders;

    public FolderApproximation(@NotNull File self, @NotNull Collection<File> childFiles, @NotNull Collection<FolderApproximation> childFolders) {
        Objects.requireNonNull(childFiles, "ChildFiles cannot be null.");
        Objects.requireNonNull(childFolders, "ChildFolders cannot be null.");
        Objects.requireNonNull(self, "Self cannot be null.");
        if (!self.isDirectory()) {
            throw new UnsupportedOperationException("Self must be a directory.");
        }
        this.self = self;
        this.childFiles = childFiles;
        this.childFolders = childFolders;
        detectChildDirs();
        detectSymLinks();
    }

    /**
     * will throw an exception if any of our childFiles is a symlink, or if {@code self} is a symlink.
     * This will include in the error message all of our direct child files that are a symlink, so the user can fix these
     */
    private void detectSymLinks() {
        if (Files.isSymbolicLink(self.toPath())) {
            throw new UnsupportedOperationException("Cannot upload symbolic links, fix these files paths: \n\t" + self.toPath());
        }
        var symPaths = childFiles
                .stream()
                .map(File::toPath)
                .filter(Files::isSymbolicLink)
                .map(Path::toAbsolutePath)
                .map(Path::toString)
                .collect(Collectors.joining("\n\t"));
        if (!symPaths.isBlank()) {
            throw new UnsupportedOperationException("Cannot upload symbolic links. Fix these file paths: \n\t" + symPaths);
        }
    }

    /**
     * will throw an exception if anything in childFiles is a directory.
     * This represents an issue with the code, rather than an issue with the user
     */
    private void detectChildDirs() {
        var childDirs = childFiles
                .stream()
                .filter(File::isDirectory)
                .map(File::toPath)
                .map(Path::toAbsolutePath)
                .map(Path::toString)
                .collect(Collectors.joining("\n\t"));
        if (!childDirs.isBlank()) {
            throw new UnsupportedOperationException("Your code is broken: directories are in the list of child files: \n\t" + childDirs);
        }
    }

    public int size() {
        int size = 1 + childFiles.size();
        for (FolderApproximation childFolder : childFolders) {
            size += childFolder.size();
        }
        return size;
    }
}
