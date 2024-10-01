package com.quarkus.vertx;

import io.vertx.mutiny.sqlclient.Row;

import java.time.ZonedDateTime;

public record Device(Long id, Integer idDevice, String sender, ZonedDateTime createdAt) {

}
