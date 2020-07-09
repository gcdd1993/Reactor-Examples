package io.github.gcdd1993.reactor.samples.core;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

public class SchedulerTest {

    @Test
    public void static_methods() {
        // 没有执行上下文, 在处理时，将直接执行提交的Runnable
        var immediateScheduler = Schedulers.immediate();
        // 单个可重用线程
        var singleScheduler = Schedulers.single();
        var singleScheduler1 = Schedulers.newSingle("test");
        // 无限制的弹性线程池
        var elasticScheduler = Schedulers.elastic();
        // 有界弹性线程池
//        var boundedElasticScheduler = Schedulers.boundedElastic();
        // 为并行工作而调整的固定工人池，它创建的工作线程数量与CPU内核数量一样多。
        var parallelScheduler = Schedulers.parallel();

        Flux.interval(Duration.ofMillis(300), Schedulers.newSingle("test"));
    }

    @Test
    public void publishOn() {
        var scheduler = Schedulers.newParallel("parallel-scheduler", 4);
        final var flux = Flux
                .range(1, 2)
                .map(i -> 10 + i)
                .publishOn(scheduler)
                .map(i -> "value " + i);

        new Thread(() -> flux.subscribe(System.out::println))
                .start();

        while (true) {
            // blocking
        }
    }

    @Test
    public void subscribeOn() {
        var scheduler = Schedulers.newParallel("parallel-scheduler", 4);
        final var flux = Flux
                .range(1, 2)
                .map(i -> 10 + i)
                .subscribeOn(scheduler)
                .map(i -> "value " + i);
        new Thread(() -> flux.subscribe(System.out::println))
                .start();

        while (true) {
            // blocking
        }
    }
}
