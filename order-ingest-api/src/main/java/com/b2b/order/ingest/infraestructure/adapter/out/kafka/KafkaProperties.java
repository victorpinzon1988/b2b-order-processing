package com.b2b.order.ingest.infraestructure.adapter.out.kafka;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.kafka")
public record KafkaProperties(
        String bootstrapServers,
        String ordersTopic
) {
}
