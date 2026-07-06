package com.b2b.worker.infraestructure.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        KafkaProperties.class,
        ExternalApisProperties.class
})
public class WorkerConfig {
}
