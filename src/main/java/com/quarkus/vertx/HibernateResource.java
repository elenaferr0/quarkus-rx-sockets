package com.quarkus.vertx;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import java.util.List;

@Path("/hibernate")
public class HibernateResource {
    @Inject
    DeviceRepository deviceRepository;

    @GET
    public Uni<List<Device>> heavy() {
        return deviceRepository.devices().emitOn(Infrastructure.getDefaultExecutor());
    }
}
