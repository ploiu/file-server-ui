package ploiu.event;

import ploiu.model.FileObject;

public class FileUpdateEvent extends Event<FileObject> {
    public FileUpdateEvent(FileObject value) {
        super(value);
    }
}
