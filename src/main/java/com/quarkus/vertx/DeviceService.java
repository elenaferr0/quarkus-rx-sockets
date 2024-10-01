package com.quarkus.vertx;

import io.quarkus.redis.datasource.ReactiveRedisDataSource;
import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.common.annotation.RunOnVirtualThread;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.net.SocketAddress;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Tuple;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@ApplicationScoped
public class DeviceService {
    @Inject
    ReactiveRedisDataSource redisDataSource;

    @Inject
    PgPool pgPool;

    @RunOnVirtualThread
    @ConsumeEvent("device.persist")
    @Transactional
    public Uni<Void> persist(List<Object> tuple) {
        Integer idDevice = (Integer) tuple.get(0);
        SocketAddress sender = (SocketAddress) tuple.get(1);

        String senderIp = sender.toString();
        ZonedDateTime createdAt = ZonedDateTime.now();
        return hibernate(idDevice, senderIp, createdAt).onItem()
                .transformToUni(x -> redis(idDevice, senderIp, createdAt));
    }

    private Uni<Void> hibernate(Integer idDevice, String senderIp, ZonedDateTime createdAt) {
        return pgPool.preparedQuery(
                "INSERT INTO devices (id_device, sender, created_at) VALUES ($1, $2, $3)"
        ).execute(
                Tuple.tuple()
                        .addInteger(idDevice)
                        .addString(senderIp)
                        .addString(createdAt.toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE))
        ).replaceWithVoid();
    }

    private Uni<Void> redis(Integer idDevice, String senderIp, ZonedDateTime createdAt) {
        return redisDataSource.json()
                .jsonSet(idDevice.toString(), "$", new Device(idDevice.longValue(), idDevice, senderIp, createdAt));
    }
}
