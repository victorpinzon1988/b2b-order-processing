package com.b2b.order.ingest.application.service;

import com.b2b.order.ingest.application.port.out.OrderMessagePublisher;
import com.b2b.order.ingest.domain.model.Order;
import com.b2b.order.ingest.domain.model.OrderItem;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.*;

public class PublishOrderServiceTest {

    private final OrderMessagePublisher publisher = mock(OrderMessagePublisher.class);
    private final PublishOrderService service = new PublishOrderService(publisher);

    @Test
    public void shouldPublishOrder(){
        Order order = sampleOrder();
        RuntimeException error = new RuntimeException("Kafka unavailable");

        when(publisher.publish(order)).thenReturn(Mono.error(error));

        StepVerifier.create(service.publish(order))
                .expectErrorMatches(ex -> ex.getMessage().equals("Kafka unavailable"))
                .verify();

        verify(publisher).publish(order);
    }

    private Order sampleOrder(){
        return new Order(
                "ORD-2026-COL-00147",
                "CLI-99821",
                "B2B",
                Instant.parse("2026-03-12T10:45:00Z"),
                List.of(new OrderItem("PRD-001", 24, new BigDecimal("3500.00")))
        );
    }
}
