package io.github.gcdd1993.reactor.samples.rsocket;

import io.rsocket.core.RSocketConnector;
import io.rsocket.transport.netty.client.WebsocketClientTransport;
import io.rsocket.util.DefaultPayload;

import java.net.URI;

/**
 * Created by gaochen on 2020/7/10.
 */
public class RSocketSampleClient {

    public static void main(String[] args) {
        var ws = WebsocketClientTransport.create(URI.create("ws://rsocket-demo.herokuapp.com/ws"));
        var clientRSocket = RSocketConnector.connectWith(ws).block();

        try {
           var s = clientRSocket.requestStream(DefaultPayload.create("peace"));
            s.take(10).doOnNext(p -> System.out.println(p.getDataUtf8())).blockLast();
        } finally {
            clientRSocket.dispose();
        }
    }
}
