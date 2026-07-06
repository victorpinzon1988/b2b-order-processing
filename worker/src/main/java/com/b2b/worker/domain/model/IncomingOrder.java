package com.b2b.worker.domain.model;

import java.time.Instant;
import java.util.List;

public record IncomingOrder(
        String orderId,
        String clientId,
        String channel,
        Instant createdAt,
        List<IncomingOrderItem> items
) {

    public boolean isInvalid(){
        return orderId == null || orderId.isBlank()
                || clientId == null || clientId.isBlank()
                || items == null || items.isEmpty()
                || items.stream().anyMatch(IncomingOrderItem::isInvalid);
    }
}
