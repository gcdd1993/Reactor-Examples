package io.github.gcdd1993.reactor.samples.r2dbc;

import io.r2dbc.spi.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static io.r2dbc.spi.ConnectionFactoryOptions.*;

public class R2dbcSample {

    private final ConnectionFactory connectionFactory;

    public R2dbcSample() {
        connectionFactory =
                ConnectionFactories.get(ConnectionFactoryOptions.builder()
                        .option(DRIVER, "postgresql")
                        .option(HOST, "10.9.108.139")
                        .option(PORT, 5432)  // optional, defaults to 5432
                        .option(USER, "maxtropy")
                        .option(PASSWORD, "maxtropy")
                        .option(DATABASE, "test_1")  // optional
                        .build());
    }

    public Mono<UserPo> createUser(UserPo userPo) {
        var baseSql = "INSERT INTO s_user(password, salt, create_time, name, creator_id, id, type, username, status) " +
                "VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9)";
        return getConnection()
                .flatMap(connection ->
                        Mono.from(connection.beginTransaction())
                                .then(Mono.from(connection.createStatement(baseSql)
                                        .bind("$1", userPo.getPassword())
                                        .bind("$2", userPo.getSalt())
                                        .bind("$3", userPo.getCreateTime())
                                        .bind("$4", userPo.getName())
                                        .bind("$5", userPo.getCreatorId())
                                        .bind("$6", userPo.getId())
                                        .bind("$7", userPo.getType())
                                        .bind("$8", userPo.getUsername())
                                        .bind("$9", userPo.getStatus())
                                        .returnGeneratedValues("id")
                                        .execute()))
                                .map(result ->
                                        result.map((row, rowMetadata) ->
                                                userPo))
                                .flatMap(Mono::from)
                                .delayUntil(__ -> {
                                    System.out.println("transaction committed");
                                    return connection.commitTransaction();
                                })
                                .doFinally(__ -> connection.close()))
                ;
    }

    public Mono<UserPo> fetch(UUID id) {
        var baseSql = "SELECT * " +
                "FROM s_user " +
                "WHERE id = $1";
        return getConnection()
                .flatMap(connection ->
                        Mono.from(connection.createStatement(baseSql)
                                .bind("$1", id.toString())
                                .execute()))
                .map(result ->
                        result.map((row, rowMetadata) ->
                                UserPo.builder()
                                        .id(row.get("id", String.class))
                                        .password(row.get("password", String.class))
                                        .salt(row.get("salt", String.class))
                                        .createTime(row.get("create_time", Long.class))
                                        .name(row.get("name", String.class))
                                        .creatorId(row.get("creator_id", String.class))
                                        .type(row.get("type", String.class))
                                        .username(row.get("username", String.class))
                                        .status(row.get("status", Integer.class))
                                        .build()))
                .flatMap(Mono::from)
                ;
    }

    public void initDatabase() {
        Flux.from(getConnection())
                .flatMap(c ->
                        Flux.from(c.createBatch()
                                .add("drop table if exists Account")
                                .add("create table Account(" +
                                        "id      bigint         NOT NULL," +
                                        "iban varchar(80) not null," +
                                        "balance DECIMAL(18,2) not null)")
                                .add("insert into Account(id,iban,balance)" +
                                        "values(1,'BR430120980198201982',100.00)")
                                .add("insert into Account(id,iban,balance)" +
                                        "values(3,'BR430120980198201982',100.00)")
                                .execute())
                                .doFinally((st) -> c.close())
                )
                .log()
                .blockLast()
        ;
    }

    public Mono<? extends Connection> getConnection() {
        return Mono.from(connectionFactory.create())
                .doOnSuccess(__ -> {
                    System.out.println("connect success");
                });
    }

}
