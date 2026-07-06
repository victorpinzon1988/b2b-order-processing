package com.b2b.worker.infraestructure.adapter.out.http;

import com.b2b.worker.application.port.out.ProductClientPort;
import com.b2b.worker.domain.model.Product;
import com.b2b.worker.infraestructure.config.ExternalApisProperties;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ProductsApiClient implements ProductClientPort {

    private final WebClient.Builder webClientBuilder;
    private final ExternalApisProperties properties;
    private final RetryRegistry retryRegistry;
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    @Override
    public Mono<Product> getProduct(String productId) {
        return webClientBuilder.baseUrl(properties.productsBaseUrl())
                .build()
                .get()
                .uri("/products/{productId}", productId)
                .retrieve()
                .bodyToMono(Product.class)
                .transformDeferred(RetryOperator.of(retryRegistry.retry("productApi")))
                .transformDeferred(CircuitBreakerOperator.of(circuitBreakerRegistry.circuitBreaker("productApi")));
    }
}
