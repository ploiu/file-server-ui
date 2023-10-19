package ploiu.model;

import org.jetbrains.annotations.NotNull;

public record FileApi(long id, @NotNull String name) implements FileObject {
}
