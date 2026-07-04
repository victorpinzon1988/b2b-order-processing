package com.b2b.order.ingest.application.port.in;

import com.b2b.order.ingest.domain.model.Order;
import reactor.core.publisher.Mono;

public interface PublishOrderUseCase {

    Mono<Void> publish(Order order);
}
