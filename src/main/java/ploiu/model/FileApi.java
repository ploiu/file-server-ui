package ploiu.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public record FileApi(long id, @NotNull String name, @NotNull Collection<TagApi> tags,
                      @Nullable Long folderId) implements FileObject {
}
