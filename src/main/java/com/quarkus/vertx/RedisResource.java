package com.quarkus.vertx;

import com.quarkus.vertx.service.RedisService;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.jboss.resteasy.reactive.RestMulti;

import java.util.List;

@Path("/redis")
public class RedisResource {
    @Inject
    RedisService redisService;

    @GET
    public Uni<Long> get() {
        return Uni.createFrom().voidItem()
                .invoke(() -> Log.info("Received1 on " + Thread.currentThread()))
                .emitOn(Infrastructure.getDefaultExecutor())
                .flatMap(x -> redisService.hash("key2", "value2"))
                .invoke(x -> Log.info("Received2: " + x + " on " + Thread.currentThread()));
    }

    @GET
    @Path("/str")
    public Uni<String> getString() {
        return Uni.createFrom().item("test")
                .onItem().invoke(x -> Log.info("Received: " + x + " on " + Thread.currentThread()))
                .runSubscriptionOn(Infrastructure.getDefaultExecutor());
    }

}
