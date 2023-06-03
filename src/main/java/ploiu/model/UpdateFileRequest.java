package ploiu.model;

import org.jetbrains.annotations.NotNull;

public record UpdateFileRequest(long id, long folderId, @NotNull String name) {
}
