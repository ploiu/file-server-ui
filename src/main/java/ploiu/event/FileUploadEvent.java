package ploiu.event;

import java.io.File;

public class FileUploadEvent extends Event<File> {
    private final long folderId;
    public FileUploadEvent(File value, long folderId) {
        super(value);
        this.folderId = folderId;
    }
}
