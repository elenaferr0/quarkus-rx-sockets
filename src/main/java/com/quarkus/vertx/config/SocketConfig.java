package com.quarkus.vertx.config;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "socket")
public interface SocketConfig {
    interface Server {
        int port();

        String host();
    }

    interface Retry {
        int maxAttempts();
    }

    Server tcp();

    Server udp();

    Retry retry();
}
