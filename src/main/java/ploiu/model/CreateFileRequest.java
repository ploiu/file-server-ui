package ploiu.model;

import org.jetbrains.annotations.NotNull;

import java.io.File;

public record CreateFileRequest(Long folderId, @NotNull File file) {
}
