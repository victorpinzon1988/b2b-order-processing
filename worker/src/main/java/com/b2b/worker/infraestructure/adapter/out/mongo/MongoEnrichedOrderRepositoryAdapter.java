package com.b2b.worker.infraestructure.adapter.out.mongo;

import com.b2b.worker.application.port.out.EnrichedOrderRepositoryPort;
import com.b2b.worker.domain.model.EnrichedOrder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class MongoEnrichedOrderRepositoryAdapter implements EnrichedOrderRepositoryPort {

    private final ReactiveEnrichedOrderMongoRepository repository;

    @Override
    public Mono<Boolean> existsProcessByOrderId(String orderId) {
        return repository.existsByOrderIdAndStatus(orderId, "PROCESSED");
    }

    @Override
    public Mono<EnrichedOrder> save(EnrichedOrder enrichedOrder) {
        EnrichedOrderDocument document = new EnrichedOrderDocument(
                null,
                enrichedOrder.orderId(),
                enrichedOrder.status(),
                enrichedOrder.client(),
                enrichedOrder.items(),
                enrichedOrder.summary(),
                enrichedOrder.processedAt()
        );

        return repository.save(document).thenReturn(enrichedOrder);
    }
}
