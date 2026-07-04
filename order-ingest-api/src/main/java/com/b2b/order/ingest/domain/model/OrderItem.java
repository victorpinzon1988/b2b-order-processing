package com.b2b.order.ingest.domain.model;

import java.math.BigDecimal;

public record OrderItem(
        String productId,
        Integer quantity,
        BigDecimal unitPrice
) {
}
