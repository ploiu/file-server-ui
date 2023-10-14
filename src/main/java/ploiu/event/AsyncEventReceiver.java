package ploiu.event;

import io.reactivex.rxjava3.core.Single;

@FunctionalInterface
public interface AsyncEventReceiver<T> {
    Single<Boolean> process(Event<T> event);
}
