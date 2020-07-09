package io.github.gcdd1993.reactor.samples.core;

import java.util.Collections;
import java.util.List;

public class MessageProcessor<T> {
    public void register(MessageListener<T> listener) {

    }

    public List<T> getHistory(long n) {
        return Collections.emptyList();
    }
}
