package io.github.gcdd1993.reactor.samples.core;

import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

public class ReactorCoreTest {

    @Test
    public void create_flux() {
        var fluxFromJust = Flux.just("foo", "bar", "foobar")
                .subscribe(System.out::println);

        var iterable = Arrays.asList("foo", "bar", "foobar");
        var fluxFromIterable = Flux.fromIterable(iterable)
                .subscribe(System.out::println);

        var fluxFromArray = Flux.fromArray(new Integer[]{1, 2, 3})
                .subscribe(System.out::println);

        var fluxFromEmpty = Flux.empty()
                .subscribe(System.out::println);

        var fluxFromRange = Flux.range(1, 10)
                .subscribe(System.out::println);

        var fluxFromInterval = Flux.interval(Duration.of(1, ChronoUnit.SECONDS))
                .subscribe(System.out::println);

    }

    @Test
    public void create_mono() {
        var monoFromSupplier = Mono
                .fromSupplier(() -> "Hello")
                .subscribe(System.out::println);

        var monoFromRunnable = Mono
                .fromRunnable(() -> {
                    // fetch from api
                    System.out.println(1111);
                }).subscribe();

        var monoFromEmpty = Mono.empty()
                .subscribe(System.out::println);

        var monoFromJust = Mono.just("foo")
                .subscribe(System.out::println);

        var monoFromRange = Flux.range(5, 3)
                .subscribe(System.out::println);

        var monoFromError = Flux.error(new IllegalAccessException("illegal access"))
                .subscribe();
    }

    @Test
    public void method_subscribe01() {
        var ints = Flux.range(1, 3);
//        ints.subscribe();

        ints.subscribe(System.out::println);
    }

    @Test
    public void method_subscribe02_with_error() {
        var ints = Flux.range(1, 4)
                .map(i -> {
                    if (i <= 3) return i;
                    throw new RuntimeException("Got to 4");
                });
        ints.subscribe(System.out::println,
                error -> System.err.println("Error: " + error));
    }

    @Test
    public void method_subscribe03() {
        var ints = Flux.range(1, 4);
        ints.subscribe(System.out::println,
                error -> System.err.println("Error: " + error),
                () -> System.out.println("Done"));
    }

    @Test
    public void method_subscribe04() {
        var ints = Flux.range(1, 4);
        ints.subscribe(System.out::println,
                error -> System.err.println("Error: " + error),
                () -> System.out.println("Done"),
                sub -> sub.request(10));
    }

    @Test
    public void sample_subscribe() {
        var sampleSubscriber = new SampleSubscriber<Integer>();
        var ints = Flux.range(1, 4);
        ints.subscribe(System.out::println,
                error -> System.err.println("Error: " + error),
                () -> System.out.println("Done"),
                sub -> sub.request(10));
        ints.subscribe(sampleSubscriber);
    }

    @Test
    public void backPressure() {
        Flux.range(1, 10)
                .doOnRequest(r -> System.out.println("request of " + r))
                .subscribe(new BaseSubscriber<Integer>() {
                    @Override
                    protected void hookOnSubscribe(Subscription subscription) {
                        request(1);
                    }

                    @Override
                    public void hookOnNext(Integer integer) {
                        System.out.println("Cancelling after having received " + integer);
                        cancel();
                    }
                });
    }

    @Test
    public void generate() {
        var flux = Flux.generate(
                () -> 0,
                (state, sink) -> {
                    sink.next("3 x " + state + " = " + 3 * state);
                    if (state == 10) sink.complete();
                    return state + 1;
                }
        ).subscribe(System.out::println);
    }

    @Test
    public void generate_mutable() {
        Flux.generate(
                AtomicLong::new,
                (state, sink) -> {
                    var i = state.getAndIncrement();
                    sink.next("3 x " + i + " = " + 3 * i);
                    if (i == 10) sink.complete();
                    return state;
                }
        ).subscribe(System.out::println);
    }

    @Test
    public void generate_consumer() {
        Flux.generate(
                AtomicLong::new,
                (state, sink) -> {
                    var i = state.getAndIncrement();
                    sink.next("3 x " + i + " = " + 3 * i);
                    if (i == 10) sink.complete();
                    return state;
                }, (state) -> System.out.println("state: " + state)
        ).subscribe(System.out::println);
    }

    @Test
    public void buffer_test() {
        Flux
                .range(1, 100)
                .buffer(20)
                .subscribe(System.out::println)
        ;

        Flux
                .interval(Duration.ofMillis(100))
                .buffer(Duration.ofMillis(1001))
                .take(2)
                .toStream()
                .forEach(System.out::println)
        ;

        Flux
                .range(1, 10)
                .bufferUntil(i -> i % 2 == 0)
                .subscribe(System.out::println)
        ;

        Flux
                .range(1, 10)
                .bufferWhile(i -> i % 2 == 0)
                .subscribe(System.out::println)
        ;
    }

    @Test
    public void filter_test() {
        Flux
                .range(1, 10)
                .filter(i -> i % 2 == 0)
                .subscribe(System.out::println)
        ;
    }

    @Test
    public void window_test() {
        // output is UnicastProcessor
        Flux
                .range(1, 100)
                .window(20)
                .subscribe(System.out::println)
        ;

        Flux
                .interval(Duration.ofMillis(100))
                .window(Duration.ofMillis(1001))
                .take(2)
                .toStream()
                .forEach(System.out::println)
        ;

    }

    @Test
    public void zipWith_test() {
        Flux
                .just("a", "b")
                .zipWith(Flux.just("c", "d"))
                .subscribe(System.out::println)
        ;

        Flux
                .just("a", "b")
                .zipWith(Flux.just("c", "d"), (s1, s2) -> String.format("%s-%s", s1, s2))
                .subscribe(System.out::println)
        ;
    }

    @Test
    public void reduce_test() {
        // 对流中包含的所有元素进行累积操作
        Flux
                .range(1, 100)
                .reduce((x, y) -> x + y)
                .subscribe(System.out::println)
        ;

        // 可以指定初始值
        Flux
                .range(1, 100)
                .reduceWith(() -> 100, (x, y) -> x + y)
                .subscribe(System.out::println)
        ;

    }

    @Test
    public void merge_test() {
        // merge按照所有流中元素的实际产生顺序来合并
        Flux
                .merge(
                        Flux.interval(Duration.ZERO, Duration.ofMillis(100)).take(5),
                        Flux.interval(Duration.ofMillis(50), Duration.ofMillis(100)).take(5)
                )
                .toStream()
                .forEach(System.out::println)
        ;

        // mergeSequential按照所有流被订阅的顺序，以流为单位进行合并。
        Flux
                .mergeSequential(
                        Flux.interval(Duration.ZERO, Duration.ofMillis(100)).take(5),
                        Flux.interval(Duration.ofMillis(50), Duration.ofMillis(100)).take(5)
                )
                .toStream()
                .forEach(System.out::println)
        ;
    }

    @Test
    public void flatMap_test() {
        // flatMap把流中的每个元素转换成一个流，再把所有流中的元素进行合并
        Flux
                .just(5, 10)
                .flatMap(x -> Flux.interval(Duration.ofMillis(x * 10), Duration.ofMillis(100)).take(x))
                .toStream()
                .forEach(System.out::println)
        ;

        // flatMapSequential把流中的每个元素转换成一个流，再把所有流中的元素进行合并
        Flux
                .just(5, 10)
                .flatMapSequential(x -> Flux.interval(Duration.ofMillis(x * 10), Duration.ofMillis(100)).take(x))
                .toStream()
                .forEach(System.out::println)
        ;

    }

    @Test
    public void concatMap_test() {
        // concatMap把流中的每个元素转换成一个流，再把所有流进行合并
        // 与 flatMap 不同的是，concatMap 会根据原始流中的元素顺序依次把转换之后的流进行合并
        // 与 flatMapSequential 不同的是，concatMap 对转换之后的流的订阅是动态进行的，而 flatMapSequential 在合并之前就已经订阅了所有的流
        Flux
                .just(5, 10)
                .concatMap(x -> Flux.interval(Duration.ofMillis(x * 10), Duration.ofMillis(100)).take(x))
                .toStream()
                .forEach(System.out::println)
        ;
    }

    @Test
    public void combineLatest_test() {
        Flux.combineLatest(
                Arrays::toString,
                Flux.interval(Duration.ofMillis(100)).take(5),
                Flux.interval(Duration.ofMillis(50), Duration.ofMillis(100)).take(5)
        )
                .log("to-stream")
                .toStream()
                .forEach(System.out::println)
        ;
    }


}
