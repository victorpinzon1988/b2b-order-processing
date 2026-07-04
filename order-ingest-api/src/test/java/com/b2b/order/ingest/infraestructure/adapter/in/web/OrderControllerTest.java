package com.b2b.order.ingest.infraestructure.adapter.in.web;

import com.b2b.order.ingest.application.port.in.PublishOrderUseCase;
import com.b2b.order.ingest.domain.model.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@WebFluxTest(OrderController.class)
public class OrderControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private PublishOrderUseCase publishOrderUseCase;

    @Test
    public void shouldReturnAcceptedWhenOrderIsValid(){
        when(publishOrderUseCase.publish(any(Order.class))).thenReturn(Mono.empty());

        webTestClient.post()
                .uri("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                         {
                              "orderId": "ORD-2026-COL-00147",
                              "clientId": "CLI-99821",
                              "channel": "B2B",
                              "createdAt": "2026-03-10T10:45:00Z",
                              "items": [
                                { "productId": "PRD-001", "quantity": 24, "unitPrice": 3500.00 }
                              ]
                          }
                        """)
                .exchange()
                .expectStatus().isAccepted()
                .expectBody()
                .jsonPath("$.orderId").isEqualTo("ORD-2026-COL-00147")
                .jsonPath("$.status").isEqualTo("Published");
    }

    @Test
    public void shouldReturnBadRequestWhenItemsAreEmpty(){
        webTestClient.post()
                .uri("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                         {
                              "orderId": "ORD-2026-COL-00147",
                              "clientId": "CLI-99821",
                              "channel": "B2B",
                              "createdAt": "2024-09-12T10:45:00Z",
                              "items": []
                            }
                        """)
                .exchange()
                .expectStatus().isBadRequest();
    }

    public void shouldReturnBadRequestWhenQuantityIsInvalid(){
        webTestClient.post()
                .uri("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                         {
                              "orderId": "ORD-2026-COL-00147",
                              "clientId": "CLI-99821",
                              "channel": "B2B",
                              "createdAt": "2024-09-12T10:45:00Z",
                              "items": [
                                { "productId": "PRD-001", "quantity": 0, "unitPrice": 3500.00 }
                              ]
                            }
                        """)
                .exchange()
                .expectStatus().isBadRequest();
    }

}
