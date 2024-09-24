package com.quarkus.vertx;

import com.quarkus.vertx.config.SocketConfig;
import com.quarkus.vertx.exceptions.ParsingException;
import com.quarkus.vertx.service.MessageService;
import io.quarkus.logging.Log;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.vertx.core.AbstractVerticle;
import io.vertx.core.net.NetServerOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.core.net.NetServer;
import io.vertx.mutiny.core.net.NetSocket;
import io.vertx.mutiny.core.net.SocketAddress;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

@ApplicationScoped
public class TCPResource extends AbstractVerticle {
    private final MessageService messageService;
    private final NetServer netServer;
    private final Vertx vertx;
    private final SocketConfig socketConfig;

    @Inject
    public TCPResource(Vertx vertx, SocketConfig socketConfig, MessageService messageService) {
        NetServerOptions options = new NetServerOptions()
                .setPort(socketConfig.tcp().port())
                .setHost(socketConfig.tcp().host());

        this.vertx = vertx;
        this.netServer = vertx.createNetServer(options);
        this.messageService = messageService;

        this.netServer.connectHandler(this::connectionHandler);
        this.netServer.listen().subscribe().with(
                x -> Log.info("TCP server is now listening on port " + x.actualPort()),
                fail -> Log.error("Failed to bind TCP server: " + fail.getCause())
        );
        this.socketConfig = socketConfig;
    }

    private void connectionHandler(NetSocket socket) {
        Log.debug("New connection from " + socket.remoteAddress());
        socket.handler(buffer -> bufferHandler(socket, buffer));
    }

    private void bufferHandler(NetSocket socket, Buffer buffer) {
        SocketAddress socketAddress = socket.remoteAddress();
        Log.debug("Received message from " + socketAddress);

        //@formatter:off
        this.messageService.handleMessage(buffer.getBytes())
                .onFailure().retry().atMost(socketConfig.retry().maxAttempts())
                .onFailure().transform(fail -> new ParsingException(fail.getMessage()))
                .onItem()
                    .invoke(r -> Log.info("Elaborated response: " + r + " for " + socketAddress))
                .onItem()
                    .transformToUni(response -> socket.write(response))
                    .invoke(v -> Log.debug("Response sent to " + socketAddress))
                .onItem()
                    .transformToUni(v -> socket.close())
                    .invoke(v -> Log.debug("Connection closed with " + socketAddress))
                .subscribe().with(
                        v -> Log.info("Response sent to " + socketAddress),
                        Throwable::printStackTrace
                );
        //@formatter:on
    }


    public void onStart(@Observes StartupEvent ev) {
        this.vertx.deployVerticle(this).subscribe().with(
                id -> Log.info("Verticle deployed: " + id),
                fail -> Log.error("Failed to deploy verticle: " + fail.getMessage())
        );
    }

    public void onShutdown(@Observes ShutdownEvent ev) {
        this.netServer.close().subscribe().with(
                v -> Log.info("TCP server closed"),
                fail -> Log.error("Failed to close TCP server: " + fail.getMessage())
        );

        this.vertx.close().subscribe().with(
                v -> Log.info("Vertx closed"),
                fail -> Log.error("Failed to close Vertx: " + fail.getMessage())
        );
    }
}
