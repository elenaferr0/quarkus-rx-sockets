package com.quarkus.vertx;

import com.quarkus.vertx.service.RedisService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/redis")
public class RedisResource {
    @Inject
    RedisService redisService;

    @GET
    public String get() {
        redisService.hash();
        return "a"; //TODO replace this stub to something useful
    }
}
