package ploiu.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.File;

@EqualsAndHashCode
public class FileUploadEvent extends Event<File> {

    @Getter
    private final long folderId;
    public FileUploadEvent(File value, long folderId) {
        super(value);
        this.folderId = folderId;
    }
}
