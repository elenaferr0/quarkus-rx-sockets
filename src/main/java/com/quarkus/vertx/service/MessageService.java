package com.quarkus.vertx.service;

import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MessageService {
    public Uni<String> handleMessage(byte[] message) {
        return Uni.createFrom().item(() -> {
            if (Math.random() > 0.5) {
                Log.info("Processing message: " + new String(message));
                return "Processed message";
            } else {
                Log.warn("Failed to process message: " + new String(message));
                throw new IllegalStateException("Failed to process message");
            }
        });
    }
}
