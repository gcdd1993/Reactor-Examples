package io.github.gcdd1993.reactor.samples.core;

import org.junit.jupiter.api.Test;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SignalType;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

public class ExceptionHandleTest {

    @Test
    public void on_error() {
        var flux = Flux
                .range(1, 10)
                .map(this::doSomethingDangerous)
                .map(this::doSomeTransform);
        flux.subscribe(
                value -> System.out.println("RECEIVED " + value),
                error -> System.err.println("CAUGHT " + error)
        );
    }

    @Test
    public void on_error_return() {
        var flux = Flux
                .just(10)
                .map(this::doSomethingDangerous)
                .onErrorReturn(100);
        flux.subscribe(
                value -> System.out.println("RECEIVED " + value),
                error -> System.err.println("CAUGHT " + error)
        );
    }

    @Test
    public void on_error_return_with_predicate() {
        var flux = Flux
                .just(10)
                .map(this::doSomethingDangerous)
                .onErrorReturn(e -> e.getMessage().contains("less than"), 100);
        flux.subscribe(
                value -> System.out.println("RECEIVED " + value),
                error -> System.err.println("CAUGHT " + error)
        );
    }

    @Test
    public void on_error_return_with_resume() {
        Flux
                .just("key1", "key2")
                .flatMap(key ->
                        callExternalService(key)
                                .onErrorResume(e -> getFromCache(key))
                );
    }

    @Test
    public void on_error_return_with_resume_predicate() {
        Flux
                .just("timeout1", "unknown", "key2")
                .flatMap(key ->
                        callExternalService(key)
                                .onErrorResume(error -> {
                                    if (error instanceof TimeoutException) {
                                        return getFromCache(key);
                                    } else if (error instanceof UnknownKeyException) {
                                        return Flux.just("DEFAULT");
                                    } else {
                                        return Flux.error(error);
                                    }
                                })

                );
    }

    @Test
    public void on_error_map() {
        Flux
                .just("timeout1", "unknown", "key2")
                .flatMap(this::callExternalService)
                .onErrorMap(original -> new BusinessException("oops, SLA exceeded", original))
                .subscribe(System.out::println);
    }

    @Test
    public void do_on_error() {
        Flux
                .just("timeout1", "unknown", "key2")
                .flatMap(this::callExternalService)
                .doOnError(error -> {
                    System.out.println("caught error");
                });
    }

    @Test
    public void do_finally() {
        Flux
                .just("foo", "bar")
                .doOnSubscribe(s -> System.out.println("start time is " + System.currentTimeMillis()))
                .doFinally(type -> {
                    if (type == SignalType.CANCEL) {
                        System.out.println("signal type is " + type);
                    }
                })
                .take(1);
    }

    @Test
    public void disposable_resource() {
        var isDisposed = new AtomicBoolean();
        var disposableInstance = new Disposable() {

            @Override
            public void dispose() {
                isDisposed.set(true);
            }

            @Override
            public String toString() {
                return "DISPOSABLE";
            }
        };

        Flux
                .using(
                        () -> disposableInstance,
                        disposable -> Flux.just(disposable.toString()),
                        Disposable::dispose
                );

    }

    @Test
    public void stop_on_error() throws InterruptedException {
        var flux = Flux
                .interval(Duration.ofMillis(250))
                .map(input -> {
                    if (input < 3) return "tick " + input;
                    throw new RuntimeException("boom");
                })
                .onErrorReturn("Uh oh");

        flux
                .subscribe(System.out::println);
        Thread.sleep(2100);
    }

    @Test
    public void retry_on_error() throws InterruptedException {
        Flux
                .interval(Duration.ofMillis(250))
                .map(input -> {
                    if (input < 3) return "tick " + input;
                    throw new RuntimeException("boom");
                })
                .retry(1)
                .elapsed()
                .subscribe(
                        System.out::println,
                        System.err::println
                );

        Thread.sleep(2100);
    }

    @Test
    public void retry_when_on_error() {
//        Flux
//                .error(new IllegalArgumentException())
//                .doOnError(System.out::println)
//                .repeatWhen(Retry.from(companion ->
//                        companion.take(3)));
    }

    @Test
    public void bi_consumer() {
        var consumer = new BiConsumer<String, String>() {

            @Override
            public void accept(String s1, String s2) {
                System.out.println(s1 + " " + s2);
            }
        }
                .andThen((s1, s2) -> System.out.println("Round1 s1: " + s1 + ", s2: " + s2))
                .andThen((s1, s2) -> System.out.println("Round2 s1: " + s1 + ", s2: " + s2))
                .andThen((s1, s2) -> System.out.println("Round3 s1: " + s1 + ", s2: " + s2))
                .andThen((s1, s2) -> System.out.println("Round4 s1: " + s1 + ", s2: " + s2));

        consumer
                .accept("s1", "s2");
    }

    private int doSomethingDangerous(int value) {
        if (value >= 6) {
            throw new RuntimeException("Value is bigger than 6");
        }
        System.out.println("doSomethingDangerous " + value);
        return value;
    }

    private int doSomeTransform(int value) {
        return value + 10;
    }

    private Flux<String> callExternalService(String key) {
        throw new RuntimeException(key + " call External Service failed");
    }

    private Flux<String> getFromCache(String key) {
        return Flux.just("value of " + key);
    }

}
