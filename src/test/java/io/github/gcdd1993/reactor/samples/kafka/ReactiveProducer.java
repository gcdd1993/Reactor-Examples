package io.github.gcdd1993.reactor.samples.kafka;

import io.github.gcdd1993.reactor.samples.fst.FstSerializer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;
import reactor.kafka.sender.SenderRecord;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ReactiveProducer {

    private static final Logger log = LoggerFactory.getLogger(ReactiveProducer.class.getName());
    private static final String BOOTSTRAP_SERVERS = "10.9.108.139:9092";

    private final KafkaSender<Integer, String> sender;

    public ReactiveProducer() {

        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "sample-producer");
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, FstSerializer.class);
        SenderOptions<Integer, String> senderOptions = SenderOptions.create(props);

        sender = KafkaSender.create(senderOptions);
    }

    public Disposable sendMessage(String topic) {
        return sender.send(this.generateMessageEvery10Min()
                .map(msg -> SenderRecord.create(new ProducerRecord<>(topic, msg.getT1(), msg.getT2()), msg.getT2())))
                .doOnError(e -> log.error("Send failed", e))
                .subscribe(r -> {
                    RecordMetadata metadata = r.recordMetadata();
                    System.out.printf("Message %s sent successfully, topic-partition=%s-%d offset=%d\n",
                            r.correlationMetadata(),
                            metadata.topic(),
                            metadata.partition(),
                            metadata.offset());
                });
    }

    public Flux<Tuple2<Integer, String>> generateMessageEvery10Min() {
        return Flux
                .interval(Duration.ofMillis(1000))
                .flatMap(x ->
                        Flux
                                .range(1, x.intValue())
                                .flatMap(__ -> Flux.just(Tuples.of(__, UUID.randomUUID().toString())))
                );
    }

    public static void main(String[] args) {
        var reactiveProducer = new ReactiveProducer();
        reactiveProducer.sendMessage("demo-topic");

        while (true) {
            // blocking to show message
        }
    }

}
