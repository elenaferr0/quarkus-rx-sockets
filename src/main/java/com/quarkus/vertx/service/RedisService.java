package com.quarkus.vertx.service;

import io.quarkus.logging.Log;
import io.quarkus.redis.datasource.ReactiveRedisDataSource;
import io.quarkus.redis.runtime.datasource.ReactiveRedisDataSourceImpl;
import io.quarkus.runtime.StartupEvent;
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

    public void hash() {
        this.reactiveRedisDataSource.hash(String.class, String.class, String.class).hset("key", Map.of("field", "value"))
                .subscribe().with(
                        x -> Log.info("Key set: " + x),
                        fail -> Log.error("Failed to set key: " + fail.getMessage())
                );

    }

    public void onStart(@Observes StartupEvent ev) {
        this.vertx.deployVerticle(this).subscribe().with(
                id -> Log.info("Verticle deployed: " + id),
                fail -> Log.error("Failed to deploy verticle: " + fail.getMessage())
        );
    }
}
