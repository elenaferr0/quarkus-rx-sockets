package com.quarkus.vertx;

import io.vertx.mutiny.sqlclient.Row;

public record Device(Long id, String code) {

    public static Device from(Row row) {
        return new Device(row.getLong("id"), row.getString("code"));
    }
}
