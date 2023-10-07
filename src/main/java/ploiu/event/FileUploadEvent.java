package ploiu.event;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.File;

@Getter
@EqualsAndHashCode(callSuper = true)
public class FileUploadEvent extends Event<File> {

    private final long folderId;

    public FileUploadEvent(File value, long folderId) {
        super(value);
        this.folderId = folderId;
    }
}
