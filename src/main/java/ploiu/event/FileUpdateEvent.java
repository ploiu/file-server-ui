package ploiu.event;

import ploiu.model.FileApi;

public class FileUpdateEvent extends Event<FileApi> {
    public FileUpdateEvent(FileApi value) {
        super(value);
    }
}
