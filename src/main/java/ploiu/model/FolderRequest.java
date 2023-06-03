package ploiu.model;


import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Represents an api request body for folder operations
 *
 * @param id       the id of the folder being operated on; if empty, means that the folder is new and doesn't currently exist
 * @param parentId the parent id of the folder, with 0 being the root
 * @param name     the name of the folder
 */
public record FolderRequest(@NotNull Optional<Long> id, long parentId, String name) {

}
