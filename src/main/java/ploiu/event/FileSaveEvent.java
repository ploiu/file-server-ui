package ploiu.event;

import lombok.Getter;
import ploiu.model.FileObject;

import java.io.File;

@Getter
public class FileSaveEvent extends Event<FileObject> {

    private final File directory;

    public FileSaveEvent(FileObject value, File directory) {
        super(value);
        if (directory == null || !directory.isDirectory()) {
            throw new UnsupportedOperationException("directory must be a directory");
        }
        this.directory = directory;
    }
}
