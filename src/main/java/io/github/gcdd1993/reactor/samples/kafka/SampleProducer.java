package io.github.gcdd1993.reactor.samples.kafka;

import io.github.gcdd1993.reactor.samples.fst.FstSerializer;
import io.github.gcdd1993.reactor.samples.fst.Person;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;
import reactor.kafka.sender.SenderRecord;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Sample producer application using Reactive API for Kafka.
 * To run sample producer
 * <ol>
 *   <li> Start Zookeeper and Kafka server
 *   <li> Update {@link #BOOTSTRAP_SERVERS} and {@link #TOPIC} if required
 *   <li> Create Kafka topic {@link #TOPIC}
 *   <li> Run {@link SampleProducer} as Java application with all dependent jars in the CLASSPATH (eg. from IDE).
 *   <li> Shutdown Kafka server and Zookeeper when no longer required
 * </ol>
 */
public class SampleProducer {

    private static final Logger log = LoggerFactory.getLogger(SampleProducer.class.getName());

    private static final String BOOTSTRAP_SERVERS = "10.9.108.139:9092";
    private static final String TOPIC = "demo-topic";

    private final KafkaSender<Long, Person> sender;
    private final SimpleDateFormat dateFormat;

    public SampleProducer(String bootstrapServers) {

        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "sample-producer");
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, FstSerializer.class);
        SenderOptions<Long, Person> senderOptions = SenderOptions.create(props);

        sender = KafkaSender.create(senderOptions);
        dateFormat = new SimpleDateFormat("HH:mm:ss:SSS z dd MMM yyyy");
    }

    public void sendMessages(String topic) throws InterruptedException {
        sender.send(Flux.interval(Duration.ofMillis(1), Schedulers.single())
                .map(i -> SenderRecord.create(new ProducerRecord<>(topic, i, person()), i)))
                .doOnError(e -> log.error("Send failed", e))
                .subscribe(r -> {
                    RecordMetadata metadata = r.recordMetadata();
                    System.out.printf("Message %d sent successfully, topic-partition=%s-%d offset=%d timestamp=%s\n",
                            r.correlationMetadata(),
                            metadata.topic(),
                            metadata.partition(),
                            metadata.offset(),
                            dateFormat.format(new Date(metadata.timestamp())));
                });
    }

    public void close() {
        sender.close();
    }

    private Person person() {
        return Person.builder()
                .name("张三")
                .age(24)
                .love("李四")
                .build();
    }

    public static void main(String[] args) throws Exception {
        SampleProducer producer = new SampleProducer(BOOTSTRAP_SERVERS);
        producer.sendMessages(TOPIC);
        while (true) {
        }
//        producer.close();
    }
}
