package io.github.gcdd1993.reactor.samples.rsocket;

import io.rsocket.AbstractRSocket;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.RSocketFactory;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.transport.netty.server.TcpServerTransport;
import io.rsocket.util.DefaultPayload;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Created by gaochen on 2020/7/10.
 */
public class ServerClientTest {

    /**
     * 请求-响应模式
     */
    @Test
    public void request_response_test() {
        RSocketFactory.receive()
                .acceptor(((setup, sendingSocket) -> Mono.just(
                        new AbstractRSocket() {
                            @Override
                            public Mono<Payload> requestResponse(Payload payload) {
                                return Mono.just(DefaultPayload.create("ECHO >> " + payload.getDataUtf8()));
                            }
                        }
                )))
                .transport(TcpServerTransport.create("localhost", 7000)) //指定传输层实现
                .start() //启动服务器
                .subscribe()
        ;

        RSocket socket = RSocketFactory.connect()
                .transport(TcpClientTransport.create("localhost", 7000)) //指定传输层实现
                .start() //启动客户端
                .block();

        socket.requestResponse(DefaultPayload.create("hello"))
                .map(Payload::getDataUtf8)
                .doOnNext(System.out::println)
                .block()
        ;

        socket.dispose();
    }

    /**
     * 请求-响应流模式
     */
    @Test
    public void request_stream_test() {
        RSocketFactory.receive()
                .acceptor(((setup, sendingSocket) -> Mono.just(
                        new AbstractRSocket() {
                            @Override
                            public Flux<Payload> requestStream(Payload payload) {
                                return Flux.fromStream(payload.getDataUtf8().codePoints().mapToObj(c -> String.valueOf((char) c))
                                        .map(DefaultPayload::create));
                            }
                        }
                )))
                .transport(TcpServerTransport.create("localhost", 7000))
                .start()
                .subscribe();

        RSocket socket = RSocketFactory.connect()
                .transport(TcpClientTransport.create("localhost", 7000))
                .start()
                .block();

        socket.requestStream(DefaultPayload.create("hello"))
                .map(Payload::getDataUtf8)
                .doOnNext(System.out::println)
                .blockLast();

        socket.dispose();
    }

    /**
     * 发后不管模式
     * <p>
     * 在发后不管模式中，由于发送方不需要等待接收方的响应，因此当程序结束时，服务器端并不一定接收到了请求。
     */
    @Test
    public void fire_and_forget_test() {
        RSocketFactory.receive()
                .acceptor(((setup, sendingSocket) -> Mono.just(
                        new AbstractRSocket() {
                            @Override
                            public Mono<Void> fireAndForget(Payload payload) {
                                System.out.println("Receive: " + payload.getDataUtf8());
                                return Mono.empty();
                            }
                        }
                )))
                .transport(TcpServerTransport.create("localhost", 7000))
                .start()
                .subscribe();

        RSocket socket = RSocketFactory.connect()
                .transport(TcpClientTransport.create("localhost", 7000))
                .start()
                .block();

        socket.fireAndForget(DefaultPayload.create("hello")).block();
        socket.fireAndForget(DefaultPayload.create("world")).block();

        socket.dispose();
    }

    /**
     * 通道模式
     */
    @Test
    public void request_tunnel_test() {
        RSocketFactory.receive()
                .acceptor(((setup, sendingSocket) -> Mono.just(
                        new AbstractRSocket() {
                            @Override
                            public Flux<Payload> requestChannel(Publisher<Payload> payloads) {
                                return Flux.from(payloads).flatMap(payload ->
                                        Flux.fromStream(
                                                payload.getDataUtf8().codePoints().mapToObj(c -> String.valueOf((char) c))
                                                        .map(DefaultPayload::create)));
                            }
                        }
                )))
                .transport(TcpServerTransport.create("localhost", 7000))
                .start()
                .subscribe();

        RSocket socket = RSocketFactory.connect()
                .transport(TcpClientTransport.create("localhost", 7000))
                .start()
                .block();

        socket.requestChannel(Flux.just("hello", "world", "goodbye").map(DefaultPayload::create))
                .map(Payload::getDataUtf8)
                .doOnNext(System.out::println)
                .blockLast();

        socket.dispose();
    }

    @Test
    public void setup_server() {
//        RSocketServer.create(new PingHandler())
//                // Enable Zero Copy
//                .payloadDecoder(PayloadDecoder.ZERO_COPY)
//                .bind(TcpServerTransport.create(7878))
//                .block()
//                .onClose()
//                .block()
//        ;
    }
}
