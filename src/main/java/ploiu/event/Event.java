package ploiu.event;

public class Event<T> {
    private final T value;

    public Event(T value) {
        this.value = value;
    }

    public T get() {
        return value;
    }
}
