package io.github.gcdd1993.reactor.samples.r2dbc;

import org.junit.jupiter.api.Test;

import java.util.UUID;

public class R2dbcTest {

    private final R2dbcSample r2dbcSample = new R2dbcSample();

    @Test
    public void connection_test() {
        r2dbcSample.getConnection()
                .doOnSuccess(__ -> System.out.println("connect database success"))
                .doOnError(error -> System.out.println("connection databse fail, " + error.getMessage()))
                .doFinally(signalType -> System.out.println("signal type " + signalType.name()))
                .subscribe();

        while (true) {
            // blocking to show result
        }
    }

    @Test
    public void select_test() {
        r2dbcSample.fetch(UUID.fromString("dd5e8e00-73ff-4062-8323-7a7815379364"))
                .doOnError(error -> System.out.println("error " + error.getMessage()))
                .subscribe(System.out::println)
        ;

        while (true) {
            // blocking to show result
        }
    }

    @Test
    public void creation_test() {
        var user = UserPo.builder()
                .password("888888")
                .username("user01")
                .type("type01")
                .creatorId("user02")
                .name("user88")
                .salt("i am salt")
                .status(0)
                .build();

        r2dbcSample.createUser(user)
                .doOnError(error -> System.out.println("error " + error.getMessage()))
                .subscribe(System.out::println)
        ;
        while (true) {
            // blocking to show result
        }
    }

    @Test
    public void batch_test() {
        r2dbcSample.initDatabase();
    }

}
