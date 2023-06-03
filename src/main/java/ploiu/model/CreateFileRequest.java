package ploiu.model;

import org.jetbrains.annotations.NotNull;

import java.io.File;

public record CreateFileRequest(long folderId, @NotNull File file) {
}
