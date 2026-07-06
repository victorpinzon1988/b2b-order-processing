package com.b2b.worker.application.port.out;

import com.b2b.worker.domain.model.EnrichedOrder;
import reactor.core.publisher.Mono;

public interface EnrichedOrderRepositoryPort {

    Mono<Boolean> existsProcessByOrderId(String orderId);
    Mono<EnrichedOrder> save(EnrichedOrder enrichedOrder);
}
