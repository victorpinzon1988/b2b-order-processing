package com.b2b.worker.application.service;

import com.b2b.worker.application.port.in.ProcessOrderUserCase;
import com.b2b.worker.application.port.out.ClientClientPort;
import com.b2b.worker.application.port.out.DeadLetterPublisherPort;
import com.b2b.worker.application.port.out.EnrichedOrderRepositoryPort;
import com.b2b.worker.application.port.out.ProductClientPort;
import com.b2b.worker.domain.model.DltMessage;
import com.b2b.worker.domain.model.IncomingOrder;
import com.b2b.worker.domain.model.Product;
import com.b2b.worker.domain.service.TaxCalculationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProcessOrderService implements ProcessOrderUserCase {

    private static final Logger log = LoggerFactory.getLogger(ProcessOrderService.class);
    private final DeadLetterPublisherPort deadLetterPublisherPort;
    private final EnrichedOrderRepositoryPort repositoryPort;
    private final ProductClientPort productClientPort;
    private final ClientClientPort clientClientPort;
    private final TaxCalculationService taxCalculationService;

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> process(String payload) {
        return parse(payload)
                .flatMap(order -> {
                        log.info("event=order_received orderId={}", order.orderId());

                        if(order.isInvalid()){
                            return sendToDlt(
                                    payload,
                                    order.orderId(),
                                    new IllegalArgumentException("Invalid order payload")
                            );
                        }

                        return repositoryPort.existsProcessByOrderId(order.orderId())
                                .flatMap(exists -> {
                                    if(exists){
                                        log.info("event=order_duplicate_skipped orderId={}", order.orderId());
                                        return Mono.empty();
                                    }

                                    return enrichAndSave(order);
                                })
                                .onErrorResume(error -> sendToDlt(payload, order.orderId(), error));
                    }

                )
                .onErrorResume(error -> sendToDlt(payload, "UNKNOWN", error));
    }

    private Mono<Void> enrichAndSave(IncomingOrder order){
        Mono<List<Product>> productsMono = Flux.fromIterable(order.items())
                .flatMap(item -> productClientPort.getProduct(item.productId()))
                .collectList();

        return Mono.zip(clientClientPort.getClient(order.clientId()), productsMono)
                .doOnSubscribe(subscription -> {
                    log.info("event=order_enrichment_started orderId={}", order.orderId());
                })
                .map(tuple -> taxCalculationService.enrich(order, tuple.getT1(), tuple.getT2()))
                .flatMap(repositoryPort::save)
                .doOnSuccess(saved ->
                        log.info("event=order_processed orderId={}", order.orderId())
                )
                .then();
    }

    private Mono<IncomingOrder> parse(String payload){
        return Mono.defer(() -> {
            try{
                IncomingOrder order = objectMapper.readValue(payload, IncomingOrder.class);
                return Mono.just(order);
            }catch (Exception error){
                return Mono.error(error);
            }
        });
    }

    private Mono<Void> sendToDlt(String payload, String orderId, Throwable error){
        log.error(
                "event=order_sent_to_dlt orderId={} errorType={} errorMessage={}",
                orderId,
                error.getClass().getSimpleName(),
                error.getMessage()
        );

        DltMessage message = new DltMessage(
                payload,
                error.getMessage(),
                error.getClass().getSimpleName(),
                1,
                Instant.now()
        );

        return deadLetterPublisherPort.publish(message);
    }
}
