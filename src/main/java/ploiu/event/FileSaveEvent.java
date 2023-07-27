package ploiu.event;

import lombok.Data;
import lombok.Getter;
import ploiu.model.FileApi;

import java.io.File;

@Getter
public class FileSaveEvent extends Event<FileApi> {

    private final File directory;

    public FileSaveEvent(FileApi value, File directory) {
        super(value);
        if (!directory.isDirectory()) {
            throw new UnsupportedOperationException("directory must be a directory");
        }
        this.directory = directory;
    }
}
