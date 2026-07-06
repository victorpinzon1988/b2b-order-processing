package com.b2b.worker.infraestructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.kafka")
public record KafkaProperties(
        String bootstrapServers,
        String ordersTopic,
        String dltTopic,
        String consumerGroup
) {
}
