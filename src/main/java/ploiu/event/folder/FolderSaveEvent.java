package ploiu.event.folder;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import ploiu.model.FolderApi;

import java.io.File;

@Getter
public class FolderSaveEvent extends FolderEvent {

    private final File saveDir;

    public FolderSaveEvent(FolderApi value, @NotNull File saveDir) {
        super(value);
        this.saveDir = saveDir;
    }
}
