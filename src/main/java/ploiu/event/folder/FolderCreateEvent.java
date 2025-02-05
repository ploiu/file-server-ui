package ploiu.event.folder;

import ploiu.model.FolderApi;

public class FolderCreateEvent extends FolderEvent {
    public FolderCreateEvent(FolderApi value) {
        super(value);
    }
}
