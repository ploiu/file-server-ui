package ploiu.event;

import lombok.Getter;
import ploiu.model.FolderApi;

public final class FolderEvent extends Event<FolderApi> {
    @Getter
    private final Type type;

    public FolderEvent(FolderApi value, Type type) {
        super(value);
        this.type = type;
    }

    public enum Type {
        CREATE, UPDATE, DELETE
    }
}
