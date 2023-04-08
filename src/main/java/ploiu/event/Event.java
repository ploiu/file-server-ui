package ploiu.event;

public final class Event<T> {
    private final T value;

    public Event(T value) {
        this.value = value;
    }

    public T get() {
        return value;
    }
}
