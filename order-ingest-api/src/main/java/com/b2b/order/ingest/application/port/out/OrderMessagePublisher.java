package com.b2b.order.ingest.application.port.out;

import com.b2b.order.ingest.domain.model.Order;
import reactor.core.publisher.Mono;

public interface OrderMessagePublisher {

    Mono<Void> publish(Order order);
}
