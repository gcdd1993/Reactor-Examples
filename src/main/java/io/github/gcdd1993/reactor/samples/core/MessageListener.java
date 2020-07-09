package io.github.gcdd1993.reactor.samples.core;

import java.util.List;

public interface MessageListener<T> {
    void onMessage(List<T> messages);
}
