package ploiu.event;

import ploiu.model.FileObject;

public class FileDeleteEvent extends Event<FileObject> {
    public FileDeleteEvent(FileObject file) {
        super(file);
    }
}
