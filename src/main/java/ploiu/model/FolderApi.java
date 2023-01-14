package ploiu.model;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public record FolderApi(
        long id,
        long parentId,
        @NotNull
        String path,
        @NotNull
        Collection<FolderApi> folders,
        @NotNull
        Collection<FileApi> files
) {
}
