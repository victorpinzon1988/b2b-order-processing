package com.b2b.worker.domain.model;

import java.time.Instant;
import java.util.List;

public record EnrichedOrder(
        String orderId,
        String status,
        Client client,
        List<EnrichedOrderItem> items,
        OrderSummary summary,
        Instant processedAt
) {
}
