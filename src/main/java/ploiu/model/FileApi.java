package ploiu.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FileApi(
        long id,
        @NotNull String name,
        @NotNull Collection<TagApi> tags,
        @Nullable Long folderId,
        // size, dateCreated, and fileType are never null when pulled back, but are read only, so passing them in create
        // or update events is useless. Therefore, `null` can be passed instead
        @Nullable Long size,
        @Nullable String dateCreated,
        @Nullable String fileType
) implements FileObject {

    public String toJson() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static FileApi fromJson(String json) {
        try {
            return new ObjectMapper().readValue(json, FileApi.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
