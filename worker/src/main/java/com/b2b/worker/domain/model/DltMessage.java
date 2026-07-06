package com.b2b.worker.domain.model;

import java.time.Instant;

public record DltMessage(
        String originalPayload,
        String errorMessage,
        String errorType,
        Integer attemptNumber,
        Instant timestamp
) {
}
