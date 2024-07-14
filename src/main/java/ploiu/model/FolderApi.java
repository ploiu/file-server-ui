package ploiu.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
) implements ServerObject {
    public String toJson() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static FolderApi fromJson(String json) {
        try {
            return new ObjectMapper().readValue(json, FolderApi.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
