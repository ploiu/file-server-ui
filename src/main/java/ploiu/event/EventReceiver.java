package ploiu.event;

@FunctionalInterface
public interface EventReceiver<T> {
    /**
     * handles the passed event. The event may or may not be processed, and the return value is used to indicate this. {@code true} means the event was accepted, and {@code false} means it was rejected
     *
     * @param event the event to handle
     * @return {@code true} if the event was processed
     */
    boolean process(Event<T> event);
}
