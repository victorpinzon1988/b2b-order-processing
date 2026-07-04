package com.b2b.order.ingest.infraestructure.adapter.out.kafka;

import com.b2b.order.ingest.application.port.out.OrderMessagePublisher;
import com.b2b.order.ingest.domain.model.Order;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.core.JsonProcessingException;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;


@Component
@RequiredArgsConstructor
public class KafkaOrderMessagePublisher implements OrderMessagePublisher {

    private final KafkaSender<String, String> kafkaSender;
    private final KafkaProperties kafkaProperties;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> publish(Order order) {
        return serialize(order)
                .flatMap(payload -> {
                    ProducerRecord<String, String> producerRecord = new ProducerRecord<>(
                            kafkaProperties.ordersTopic(),
                            order.orderId(),
                            payload
                    );

                    SenderRecord<String, String, String> senderRecord =
                            SenderRecord.create(producerRecord, order.orderId());

                    return kafkaSender.send(Mono.just(senderRecord)).then();
                });
    }

    private Mono<String> serialize(Order order){
        return Mono.fromCallable(() -> objectMapper.writeValueAsString(order))
                .onErrorMap(JsonProcessingException.class, error ->
                        new IllegalStateException("Could not serialize order: " + order.orderId()));
    }
}
