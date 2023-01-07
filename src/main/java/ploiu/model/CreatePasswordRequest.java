package ploiu.model;

import org.jetbrains.annotations.NotNull;

public record CreatePasswordRequest(@NotNull String username, @NotNull String password) {
}
