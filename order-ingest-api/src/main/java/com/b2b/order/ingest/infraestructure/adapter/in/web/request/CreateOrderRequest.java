package com.b2b.order.ingest.infraestructure.adapter.in.web.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;

public record CreateOrderRequest(
        @NotBlank String orderId,
        @NotBlank String clientId,
        @NotBlank String channel,
        @NotNull Instant createdAt,
        @NotEmpty List<@Valid CreateOrderItemRequest> items

) {
}
