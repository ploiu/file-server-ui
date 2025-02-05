package ploiu.event.folder;

import ploiu.model.FolderApi;

public class FolderDeleteEvent extends FolderEvent {
    public FolderDeleteEvent(FolderApi value) {
        super(value);
    }
}
