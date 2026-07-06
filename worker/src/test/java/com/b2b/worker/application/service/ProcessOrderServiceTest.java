package com.b2b.worker.application.service;

import com.b2b.worker.application.port.out.ClientClientPort;
import com.b2b.worker.application.port.out.DeadLetterPublisherPort;
import com.b2b.worker.application.port.out.EnrichedOrderRepositoryPort;
import com.b2b.worker.application.port.out.ProductClientPort;
import com.b2b.worker.domain.model.*;
import com.b2b.worker.domain.service.TaxCalculationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ProcessOrderServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private final ProductClientPort productClientPort = mock(ProductClientPort.class);
    private final ClientClientPort clientClientPort = mock(ClientClientPort.class);
    private final EnrichedOrderRepositoryPort repositoryPort = mock(EnrichedOrderRepositoryPort.class);
    private final DeadLetterPublisherPort deadLetterPublisherPort = mock(DeadLetterPublisherPort.class);

    private final ProcessOrderService service = new ProcessOrderService(
            deadLetterPublisherPort,
            repositoryPort,
            productClientPort,
            clientClientPort,
            new TaxCalculationService(),
            objectMapper
    );

    @Test
    void shouldSkipOrderWhenAlreadyProcessed() {
        String payload = """
                {
                  "orderId": "ORD-001",
                  "clientId": "CLI-99821",
                  "channel": "B2B",
                  "createdAt": "2024-09-12T10:45:00Z",
                  "items": [
                    { "productId": "PRD-001", "quantity": 24, "unitPrice": 3500.00 }
                  ]
                }
                """;

        when(repositoryPort.existsProcessByOrderId("ORD-001")).thenReturn(Mono.just(true));

        StepVerifier.create(service.process(payload))
                .verifyComplete();

        verify(repositoryPort).existsProcessByOrderId("ORD-001");
        verifyNoInteractions(productClientPort);
        verifyNoInteractions(clientClientPort);
        verify(repositoryPort, never()).save(any());
        verifyNoInteractions(deadLetterPublisherPort);
    }

    @Test
    void shouldSendInvalidOrderToDlt() {
        String payload = """
                {
                  "orderId": "ORD-INVALID",
                  "clientId": "CLI-99821",
                  "channel": "B2B",
                  "createdAt": "2026-02-23T10:45:00Z",
                  "items": []
                }
                """;

        when(deadLetterPublisherPort.publish(any(DltMessage.class))).thenReturn(Mono.empty());

        StepVerifier.create(service.process(payload))
                .verifyComplete();

        verify(deadLetterPublisherPort).publish(any(DltMessage.class));
        verifyNoInteractions(productClientPort);
        verifyNoInteractions(clientClientPort);
        verify(repositoryPort, never()).save(any());
    }
}
