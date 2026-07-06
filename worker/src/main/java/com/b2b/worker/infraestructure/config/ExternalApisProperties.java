package com.b2b.worker.infraestructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.external-apis")
public record ExternalApisProperties(
        String productsBaseUrl,
        String clientsBaseUrl
) {
}
