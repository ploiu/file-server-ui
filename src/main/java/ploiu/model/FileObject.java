package ploiu.model;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * marker interface to help get around the limitations of the api
 */
public interface FileObject extends ServerObject, Serializable {
    long id();

    @NotNull String name();
}
