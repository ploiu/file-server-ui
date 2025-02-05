package ploiu.event.folder;

import ploiu.model.FolderApi;

public class FolderUpdateEvent extends FolderEvent {
    public FolderUpdateEvent(FolderApi value) {
        super(value);
    }
}
