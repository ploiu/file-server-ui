package ploiu.model;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * This is a hacky way to not break our event handler implementation.
 * The server side will need to include parent folder id in its FileApi implementation
 */
public record FileApiWithFolder(long id, @NotNull String name, Optional<Long> folderId) implements FileObject {
}
