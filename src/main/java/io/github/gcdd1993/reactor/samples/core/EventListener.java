package io.github.gcdd1993.reactor.samples.core;

import java.util.List;

public interface EventListener<T> {
    void onDataChunk(List<T> chunk);

    void processComplete();
}
