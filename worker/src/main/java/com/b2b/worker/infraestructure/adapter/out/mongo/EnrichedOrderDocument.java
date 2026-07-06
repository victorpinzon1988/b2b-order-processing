package com.b2b.worker.infraestructure.adapter.out.mongo;

import com.b2b.worker.domain.model.Client;
import com.b2b.worker.domain.model.EnrichedOrderItem;
import com.b2b.worker.domain.model.OrderSummary;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document(collection = "enriched_orders")
public record EnrichedOrderDocument(
        String id,
        String orderId,
        String status,
        Client client,
        List<EnrichedOrderItem> items,
        OrderSummary summary,
        Instant processedAt
){

}
