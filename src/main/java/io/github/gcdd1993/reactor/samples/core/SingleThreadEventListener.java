package io.github.gcdd1993.reactor.samples.core;

public interface SingleThreadEventListener<T> extends EventListener<T> {
    void processError(Throwable e);
}
