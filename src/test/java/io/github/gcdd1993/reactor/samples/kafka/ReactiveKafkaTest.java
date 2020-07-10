package io.github.gcdd1993.reactor.samples.kafka;

import org.junit.jupiter.api.Test;

public class ReactiveKafkaTest {

    @Test
    public void test01() {
        var reactiveConsumer = new ReactiveConsumer();
        reactiveConsumer.consume();

        var reactiveProducer = new ReactiveProducer();
        reactiveProducer.sendMessage("demo-topic");

        while (true) {
            // blocking to show message
        }
    }

}
