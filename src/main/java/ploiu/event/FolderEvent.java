package ploiu.event;

import lombok.Getter;
import ploiu.model.FolderApi;

@Getter
public final class FolderEvent extends Event<FolderApi> {
    private final Type type;

    public FolderEvent(FolderApi value, Type type) {
        super(value);
        this.type = type;
    }

    public enum Type {
        CREATE, UPDATE, DELETE
    }
}
