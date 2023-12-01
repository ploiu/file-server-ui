package ploiu.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Collection;

public record FolderApi(
        long id,
        long parentId,
        String name,
        // does not update api, will be empty when creating a folder
        @Nullable
        String path,
        @NotNull
        Collection<FolderApi> folders,
        @NotNull
        Collection<FileApi> files,
        @NotNull
        Collection<TagApi> tags
) implements ServerObject, Serializable {
}
