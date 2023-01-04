package ploiu.model;

import java.util.Collection;

public record FolderApi(long id, long parentId, String path, Collection<FolderApi> folders, Collection<FileApi> files) {
}
