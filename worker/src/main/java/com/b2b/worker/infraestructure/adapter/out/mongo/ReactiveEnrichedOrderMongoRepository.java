package com.b2b.worker.infraestructure.adapter.out.mongo;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface ReactiveEnrichedOrderMongoRepository extends ReactiveMongoRepository<EnrichedOrderDocument, String> {
    Mono<Boolean> existsByOrderIdAndStatus(String orderId, String status);
}
