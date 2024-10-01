package com.quarkus.vertx.service;

import io.quarkus.logging.Log;
import io.quarkus.redis.datasource.ReactiveRedisDataSource;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.vertx.core.AbstractVerticle;
import io.vertx.mutiny.core.Vertx;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

import java.util.Map;

@ApplicationScoped
public class RedisService extends AbstractVerticle {
    private final ReactiveRedisDataSource reactiveRedisDataSource;
    private final Vertx vertx;

    @Inject
    public RedisService(ReactiveRedisDataSource reactiveRedisDataSource, Vertx vertx) {
        this.reactiveRedisDataSource = reactiveRedisDataSource;
        this.vertx = vertx;
    }

    public Uni<Long> hash(String key, String value) {
        Log.info(Thread.currentThread());

        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            Log.error("Thread interrupted: " + e.getMessage());
        }
        return this.reactiveRedisDataSource.hash(String.class).hset(key, Map.of(key, value));
    }

    public void onStart(@Observes StartupEvent ev) {
        this.vertx.deployVerticle(this).subscribe().with(
                id -> Log.info("Verticle deployed: " + id),
                fail -> Log.error("Failed to deploy verticle: " + fail.getMessage())
        );
    }
}
