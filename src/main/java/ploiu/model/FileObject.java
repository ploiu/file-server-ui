package ploiu.model;

import org.jetbrains.annotations.NotNull;

/**
 * marker interface to help get around the limitations of the api
 */
public interface FileObject extends ServerObject {
    long id();

    @NotNull
    String name();
}
