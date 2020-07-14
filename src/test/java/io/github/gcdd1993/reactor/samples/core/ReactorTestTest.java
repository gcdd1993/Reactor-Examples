package io.github.gcdd1993.reactor.samples.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;

import java.time.Duration;

/**
 * Created by gaochen on 2020/7/13.
 */
public class ReactorTestTest {

    private final Flux<String> source = Flux.just("John", "Monica", "Mark", "Cloe", "Frank", "Casper", "Olivia", "Emily", "Cate")
            .filter(name -> name.length() == 4)
            .map(String::toUpperCase);

    private final Flux<Integer> source01 = Flux
            .<Integer>create(emitter -> {
                emitter.next(1);
                emitter.next(2);
                emitter.next(3);
                emitter.complete();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                emitter.next(4);
            })
            .filter(number -> number % 2 == 0);

    @Test
    public void stepVerifierTest() {
        source.log()
                .subscribe(System.out::println);

        StepVerifier
                .create(source)
                .expectNext("JOHN")
                .expectNextMatches(name -> name.startsWith("MA"))
                .expectNext("CLOE", "CATE")
                .expectComplete()
                .verify()
        ;
    }

    @Test
    public void stepVerifierWithExceptionTest() {
        var error = source
                .concatWith(Mono.error(new IllegalArgumentException("Our message")));

        StepVerifier
                .create(error)
                .expectNextCount(4)
                .expectError()
                .verify()
        ;

        StepVerifier
                .create(error)
                .expectNextCount(4)
                .expectError(IllegalArgumentException.class)
                .verify()
        ;

        StepVerifier
                .create(error)
                .expectNextCount(4)
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException &&
                                throwable.getMessage().equals("Our message")
                ).verify()
        ;

        StepVerifier
                .create(error)
                .expectNextCount(4)
                .expectErrorSatisfies(ex -> {
                    Assertions.assertEquals(ex.getMessage(), "Our message");
                })
                .verify()
        ;

    }

    @Test
    public void timeBasedPublishersTest() {
        StepVerifier
                .withVirtualTime(() -> Flux.interval(Duration.ofSeconds(1)).take(2))
                .expectSubscription()
                .expectNoEvent(Duration.ofSeconds(1))
                .expectNext(0L)
                .thenAwait(Duration.ofSeconds(1))
                .expectNext(1L)
                .verifyComplete()
        ;
    }

    @Test
    public void postExecutionTest() {
        StepVerifier
                .create(source01)
                .expectNext(2)
                .expectComplete()
                .verifyThenAssertThat()
                .hasDropped(4)
                .tookLessThan(Duration.ofMillis(1050))
        ;
    }

    @Test
    public void produceDataTest() {
        // send one or more signals to subscribers
        TestPublisher
                .<String>create()
                .next("First", "Second", "Third")
                .error(new RuntimeException("Message"))
        ;

        // same as next(T) but invokes complete() afterwards
        TestPublisher
                .<String>create()
                .emit("First", "Second", "Third")
                .error(new RuntimeException("Message"))
        ;

        //  terminates a source with the complete signal
        TestPublisher
                .<String>create()
                .next("First")
                .complete()
        ;

        TestPublisher
                .<String>create()
                .emit("First", "Second", "Third")
                .flux()
                .subscribe(System.out::println)
        ;

        TestPublisher
                .<String>create()
                .emit("First", "Second", "Third")
                .mono()
                .subscribe(System.out::println)
        ;
    }

    @Test
    public void testPublisher() {
        final var testPublisher = TestPublisher.<String>create();
        var uppercaseConverter = new UppercaseConverter(testPublisher.flux());

        StepVerifier
                .create(uppercaseConverter.getUppercase())
                .then(() -> testPublisher.emit("aA", "bb", "ccc"))
                .expectNext("AA", "BB", "CCC")
                .verifyComplete()
        ;

    }

    @Test
    public void misbehavingPublisherTest() {
        TestPublisher
                .createNoncompliant(TestPublisher.Violation.ALLOW_NULL)
                .emit("1", "2", null, "3")
        ;
    }

    public static class UppercaseConverter {
        private final Flux<String> source;

        public UppercaseConverter(Flux<String> source) {
            this.source = source;
        }

        Flux<String> getUppercase() {
            return source
                    .map(String::toUpperCase)
                    ;
        }

    }

}
