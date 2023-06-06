package ploiu.event;

import ploiu.model.FileApi;

public class FileDeleteEvent extends Event<FileApi> {
    public FileDeleteEvent(FileApi file) {
        super(file);
    }
}
