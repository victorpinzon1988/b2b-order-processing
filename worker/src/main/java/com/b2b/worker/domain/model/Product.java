package com.b2b.worker.domain.model;

public record Product(
        String productId,
        String name,
        String sku,
        String category,
        TaxCategory taxCategory,
        String unitOfMeasure
) {
}
