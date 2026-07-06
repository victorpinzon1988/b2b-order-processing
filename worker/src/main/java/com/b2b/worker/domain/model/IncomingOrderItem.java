package com.b2b.worker.domain.model;

import java.math.BigDecimal;

public record IncomingOrderItem(
        String productId,
        Integer quantity,
        BigDecimal unitPrice
) {
    public boolean isInvalid(){
        return productId == null || productId.isBlank()
                || quantity == null || quantity <= 0
                || unitPrice == null || unitPrice.signum() < 0;
    }
}
