package ploiu.model;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record TagApi(@NotNull Optional<String> id, @NotNull String title) {
}
