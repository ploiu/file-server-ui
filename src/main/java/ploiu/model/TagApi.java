package ploiu.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

public record TagApi(@Nullable Long id, @NotNull String title, @Nullable Long implicitFrom) implements Serializable {
}
