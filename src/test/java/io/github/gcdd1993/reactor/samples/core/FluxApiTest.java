package io.github.gcdd1993.reactor.samples.core;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public class FluxApiTest {

    @Test
    public void create_from_listener() {
        var eventProcessor = new EventProcessor<String>();
        var bridge = Flux.create(sink -> {
            eventProcessor.register(new EventListener<>() {
                @Override
                public void onDataChunk(List<String> chunk) {
                    for (String s : chunk) {
                        sink.next(s);
                    }
                }

                @Override
                public void processComplete() {
                    sink.complete();
                }
            });
        });
    }

    @Test
    public void generate_from_listener() {
        var eventProcessor = new EventProcessor<String>();
        Flux.push(sink -> {
            eventProcessor.register(new SingleThreadEventListener<>() {

                @Override
                public void onDataChunk(List<String> chunk) {
                    for (String s : chunk) {
                        sink.next(s);
                    }
                }

                @Override
                public void processComplete() {
                    sink.complete();
                }

                @Override
                public void processError(Throwable e) {
                    sink.error(e);
                }
            });
        });
    }

    @Test
    public void create_from_message_listener() {
        var messageProcessor = new MessageProcessor<String>();
        Flux.create(sink -> {
            messageProcessor.register(messages -> {
                for (String s : messages) {
                    sink.next(s);
                }
            });
            sink.onRequest(n -> {
                List<String> messages = messageProcessor.getHistory(n);
                for (String s : messages) {
                    sink.next(s);
                }
            });
        });
    }

    @Test
    public void handle_safe_delete_null_value() {
        Flux.just(-1, 30, 13, 9, 20)
                .handle((i, sink) -> {
                    var letter = alphabet(i);
                    if (letter != null) {
                        sink.next(letter);
                    }
                }).subscribe(System.out::println);
    }

    @Test
    public void mono_in_new_thread() throws InterruptedException {
        var mono = Mono.just("hello");
        var thread = new Thread(() ->
                mono
                        .map(msg -> msg + " thread ")
                        .subscribe(v -> System.out.println(v + Thread.currentThread().getName())));
        thread.start();
        thread.join();
    }

    private String alphabet(int letterNumber) {
        if (letterNumber < 1 || letterNumber > 26) {
            return null;
        }
        int letterIndexAscii = 'A' + letterNumber - 1;
        return "" + (char) letterIndexAscii;
    }

}
