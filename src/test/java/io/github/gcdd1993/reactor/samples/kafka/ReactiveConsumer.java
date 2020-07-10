package io.github.gcdd1993.reactor.samples.kafka;

import io.github.gcdd1993.reactor.samples.fst.FstDeserializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;
import reactor.core.scheduler.Schedulers;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOffset;
import reactor.kafka.receiver.ReceiverOptions;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by gaochen on 2020/7/9.
 */
public class ReactiveConsumer {

    private static final Logger log = LoggerFactory.getLogger(SampleConsumer.class.getName());

    private static final String BOOTSTRAP_SERVERS = "10.9.108.139:9092";
    private static final String TOPIC = "demo-topic";

    private final ReceiverOptions<Integer, String> receiverOptions;

    public ReactiveConsumer() {

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, "sample-consumer");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "sample-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, IntegerDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, FstDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        receiverOptions = ReceiverOptions.create(props);
    }

    public Disposable consume() {
        var options = receiverOptions.subscription(Collections.singleton(TOPIC))
                .addAssignListener(partitions -> log.debug("onPartitionsAssigned {}", partitions))
                .addRevokeListener(partitions -> log.debug("onPartitionsRevoked {}", partitions));
        var kafkaFlux = KafkaReceiver.create(options).receive();
        return kafkaFlux
                .subscribe(record -> {
                    ReceiverOffset offset = record.receiverOffset();
                    log.info("Received message: topic-partition={} offset={} key={} value={}",
                            offset.topicPartition(),
                            offset.offset(),
                            record.key(),
                            record.value());
                    offset.acknowledge();
                });
    }

    public Disposable consumeWithBlocking() {
        var options = receiverOptions.subscription(Collections.singleton(TOPIC))
                .addAssignListener(partitions -> log.debug("onPartitionsAssigned {}", partitions))
                .addRevokeListener(partitions -> log.debug("onPartitionsRevoked {}", partitions));
        var kafkaFlux = KafkaReceiver.create(options).receive();
        return kafkaFlux
                .parallel()
                .runOn(Schedulers.parallel())
                .subscribe(record -> {
                    ReceiverOffset offset = record.receiverOffset();
                    log.info("Received message: topic-partition={} offset={} key={} value={}",
                            offset.topicPartition(),
                            offset.offset(),
                            record.key(),
                            record.value());
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    offset.acknowledge();
                });
    }

    public static void main(String[] args) {
        var consumer = new ReactiveConsumer();
        consumer.consumeWithBlocking();
        while (true) {
            // blocking to show message
        }
    }

}
