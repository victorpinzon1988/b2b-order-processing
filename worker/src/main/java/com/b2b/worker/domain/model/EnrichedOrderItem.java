package com.b2b.worker.domain.model;

import java.math.BigDecimal;

public record EnrichedOrderItem(
        String productId,
        String name,
        String sku,
        TaxCategory taxCategory,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal,
        BigDecimal taxRate,
        BigDecimal taxAmount,
        BigDecimal lineTotal
) {
}
