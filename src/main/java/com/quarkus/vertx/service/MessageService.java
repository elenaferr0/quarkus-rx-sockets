package com.quarkus.vertx.service;

import com.quarkus.vertx.DeviceRepository;
import com.quarkus.vertx.DeviceService;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.vertx.mutiny.core.eventbus.EventBus;
import io.vertx.mutiny.core.net.SocketAddress;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Duration;
import java.util.List;

@ApplicationScoped
public class MessageService {
    @Inject
    DeviceService deviceService;

    @Inject
    EventBus eventBus;

    public Uni<String> handleMessage(byte[] message, SocketAddress socketAddress) {
        String response = new String(message).toUpperCase();
        Integer idDevice = Integer.parseInt(response);
        Log.info("Received message: " + response);

        return wait(idDevice, Duration.ofSeconds(1))
//                .onItem().transformToUni(x -> deviceService.persist(x, socketAddress))
                // emit on event bus
//                .emitOn(Infrastructure.getDefaultExecutor())
                .onItem().transformToUni(x -> eventBus.<Void>request("device.persist", List.of(idDevice, socketAddress)))
                .onFailure().invoke(Throwable::printStackTrace)
                .map((d) -> "OK");
    }

    public Uni<Integer> wait(Integer result, Duration duration) {
        return Uni.createFrom().item(result).onItem().delayIt().by(duration);
    }
}
