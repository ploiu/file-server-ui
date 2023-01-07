package ploiu.model;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record UpdateFileRequest(long id, @NotNull Optional<Long> folderId, @NotNull String name) {
}
