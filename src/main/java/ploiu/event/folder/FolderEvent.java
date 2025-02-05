package ploiu.event.folder;

import ploiu.event.Event;
import ploiu.model.FolderApi;

/**
 * marker class. Do not use directly, instead use teh classes that extend this one
 */
public abstract class FolderEvent extends Event<FolderApi> {

    protected FolderEvent(FolderApi value) {
        super(value);
    }
}
