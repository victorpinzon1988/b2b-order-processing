package com.b2b.worker.infraestructure.adapter.out.kafka;

import com.b2b.worker.application.port.out.DeadLetterPublisherPort;
import com.b2b.worker.domain.model.DltMessage;
import com.b2b.worker.infraestructure.config.KafkaProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

@Component
@RequiredArgsConstructor
public class KafkaDeadLetterPublisherAdapter implements DeadLetterPublisherPort {

    private final KafkaSender<String, String> kafkaSender;
    private final KafkaProperties kafkaProperties;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> publish(DltMessage message) {
        return Mono.fromCallable(() -> objectMapper.writeValueAsString(message))
                .flatMap(payload -> {
                    ProducerRecord<String, String> record = new ProducerRecord<>(
                            kafkaProperties.dltTopic(),
                            null,
                            payload
                    );

                    return kafkaSender.send(Mono.just(SenderRecord.create(record, "dlt"))).then();
                });
    }
}
