package com.quarkus.vertx;

import com.quarkus.vertx.config.SocketConfig;
import com.quarkus.vertx.service.MessageService;
import io.quarkus.logging.Log;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.vertx.core.AbstractVerticle;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.datagram.DatagramPacket;
import io.vertx.mutiny.core.datagram.DatagramSocket;
import io.vertx.mutiny.core.net.SocketAddress;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

@ApplicationScoped
public class UDPResource extends AbstractVerticle {
    private final DatagramSocket datagramSocket;
    private final MessageService messageService;
    private final Vertx vertx;
    private final SocketConfig socketConfig;

    @Inject
    public UDPResource(Vertx vertx, SocketConfig socketConfig, MessageService messageService) {
        this.vertx = vertx;
        this.messageService = messageService;
        this.datagramSocket = vertx.createDatagramSocket();
        this.datagramSocket.handler(this::packetHandler);
        this.datagramSocket.listen(socketConfig.udp().port(), socketConfig.udp().host()).subscribe().with(
                x -> Log.info("UDP server is now listening on port " + socketConfig.udp().port()),
                fail -> Log.error("Failed to bind UDP server: " + fail.getMessage())
        );
        this.socketConfig = socketConfig;
    }

    private void packetHandler(DatagramPacket packet) {
        SocketAddress sender = packet.sender();
        Log.debug("Received message from " + sender);
        //@formatter:off
        this.messageService.handleMessage(packet.data().getBytes())
                .onFailure().retry().atMost(socketConfig.retry().maxAttempts())
                .onItem()
                    .invoke(r -> Log.info("Elaborated response: " + r + " for " + sender))
                .onItem()
                    .transformToUni(response -> this.datagramSocket.send(response, sender.port(), sender.host()))
                    .invoke(v -> Log.debug("Response sent to " + sender))
                .subscribe().with(
                        v -> Log.info("Message sent to " + sender),
                        fail -> Log.error("Failed to send message to " + sender + ": " + fail.getMessage())
                );
        //@formatter:on
    }

    public void onStart(@Observes StartupEvent ev) {
        this.vertx.deployVerticle(this).subscribe().with(
                id -> Log.info("Verticle " + id + " deployed"),
                fail -> Log.error("Failed to deploy verticle: " + fail.getMessage())
        );
    }

    public void onShutdown(@Observes ShutdownEvent ev) {
        this.datagramSocket.close().subscribe().with(
                x -> Log.info("Datagram socket closed"),
                fail -> Log.error("Failed to close datagram socket: " + fail.getMessage())
        );

        this.vertx.close().subscribe().with(
                x -> Log.info("Vertx closed"),
                fail -> Log.error("Failed to close Vertx: " + fail.getMessage())
        );
    }

}
