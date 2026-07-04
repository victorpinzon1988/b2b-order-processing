package com.b2b.order.ingest.domain.model;

import java.time.Instant;
import java.util.List;

public record Order(
        String orderId,
        String clientId,
        String channel,
        Instant createdAt,
        List<OrderItem> items
) {
}
