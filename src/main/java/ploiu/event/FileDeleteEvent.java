package ploiu.event;

import ploiu.model.FileApi;

import java.io.File;

public class FileDeleteEvent extends Event<FileApi> {
    public FileDeleteEvent(FileApi file) {
        super(file);
    }
}
