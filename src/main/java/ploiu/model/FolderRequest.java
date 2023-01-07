package ploiu.model;


import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record FolderRequest(@NotNull Optional<Long> id, @NotNull Optional<Long> parentId, String name) {

}
