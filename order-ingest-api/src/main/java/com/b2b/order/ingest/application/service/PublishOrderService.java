package com.b2b.order.ingest.application.service;

import com.b2b.order.ingest.application.port.in.PublishOrderUseCase;
import com.b2b.order.ingest.application.port.out.OrderMessagePublisher;
import com.b2b.order.ingest.domain.model.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class PublishOrderService implements PublishOrderUseCase {

    private final OrderMessagePublisher orderMessagePublisher;


    @Override
    public Mono<Void> publish(Order order) {
        return orderMessagePublisher.publish(order);
    }
}
