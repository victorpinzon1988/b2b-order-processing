package com.b2b.worker.infraestructure.adapter.out.http;

import com.b2b.worker.application.port.out.ClientClientPort;
import com.b2b.worker.domain.model.Client;
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
public class ClientsApiClient implements ClientClientPort {

    private final WebClient.Builder webClientBuilder;
    private final ExternalApisProperties properties;
    private final RetryRegistry retryRegistry;
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    @Override
    public Mono<Client> getClient(String clientId) {
        return webClientBuilder.baseUrl(properties.clientsBaseUrl())
                .build()
                .get()
                .uri("/clients/{clientId}", clientId)
                .retrieve()
                .bodyToMono(Client.class)
                .transformDeferred(RetryOperator.of(retryRegistry.retry("clientsApi")))
                .transformDeferred(CircuitBreakerOperator.of(circuitBreakerRegistry.circuitBreaker("clientsApi")));

    }
}
