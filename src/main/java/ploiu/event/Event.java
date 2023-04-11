package ploiu.event;

public sealed class Event<T> permits FolderEvent {
    private final T value;

    public Event(T value) {
        this.value = value;
    }

    public T get() {
        return value;
    }
}
