package com.b2b.worker.domain.model;

public record Client(
        String clientId,
        String name,
        String segment,
        String taxRegime,
        String region
) {
}
