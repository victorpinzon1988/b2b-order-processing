package com.b2b.order.ingest.infraestructure.adapter.in.web;

import com.b2b.order.ingest.application.port.in.PublishOrderUseCase;
import com.b2b.order.ingest.infraestructure.adapter.in.web.request.CreateOrderRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final PublishOrderUseCase publishOrderUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Mono<Map<String, String>> publish(@Valid @RequestBody CreateOrderRequest request){
        return publishOrderUseCase.publish(OrderRequestMapper.toDomain(request))
                .thenReturn(Map.of(
                        "orderId", request.orderId(),
                        "status", "Published"
                ));
    }
}
