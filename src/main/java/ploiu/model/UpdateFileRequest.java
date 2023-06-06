package ploiu.model;

import org.jetbrains.annotations.NotNull;

// TODO consolidate this with the other file requests like with Folder request?
public record UpdateFileRequest(long id, long folderId, @NotNull String name) {
}
