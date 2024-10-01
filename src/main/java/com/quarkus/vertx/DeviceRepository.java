package com.quarkus.vertx;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class DeviceRepository {
    @Inject
    PgPool client;

    public Uni<List<Device>> devices() {
        Uni<RowSet<Row>> queryExecution = client.query("SELECT * FROM devices").execute();
        return queryExecution.onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
                .collect().asList()
                .onItem().transform((List<Row> rows) -> rows.stream().map(Device::from).toList());
    }
}
