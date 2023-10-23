package ploiu.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public record CreateFileRequest(long folderId, @NotNull File file, @JsonIgnore boolean force) {
    public CreateFileRequest(long folderId, @NotNull File file) {
        this(folderId, file, false);
    }
}
