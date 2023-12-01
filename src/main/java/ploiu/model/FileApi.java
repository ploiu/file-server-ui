package ploiu.model;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public record FileApi(long id, @NotNull String name, @NotNull Collection<TagApi> tags) implements FileObject {
}
