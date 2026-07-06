package com.b2b.worker.infraestructure.adapter.in.kafka;

import com.b2b.worker.application.port.in.ProcessOrderUserCase;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.kafka.receiver.KafkaReceiver;


@Component
@RequiredArgsConstructor
public class KafkaOrderConsumer {

    private static final Logger log = LoggerFactory.getLogger(KafkaOrderConsumer.class);

    private final KafkaReceiver<String, String> kafkaReceiver;
    private final ProcessOrderUserCase processOrderUserCase;

    @PostConstruct
    void consume(){
        kafkaReceiver.receive()
                .flatMap(record -> {
                    log.info(
                        "event=kafka_message_received topic={} partition={} offset={} key={}",
                        record.topic(),
                        record.partition(),
                        record.offset(),
                        record.key()
                    );

                    return processOrderUserCase.process(record.value())
                            .then(record.receiverOffset().commit())
                            .doOnSuccess(ignored -> log.info(
                                "event=kafka_offset_committed topic={} partition={} offset={}",
                                record.topic(),
                                record.partition(),
                                record.offset()
                            ));
                })
                .doOnError(error -> log.error("event=kafka_consumer_error errorType={} errorMessage={}",
                        error.getClass().getSimpleName(),
                        error.getMessage()
                ))
                .retry()
                .subscribe();
    }
}
