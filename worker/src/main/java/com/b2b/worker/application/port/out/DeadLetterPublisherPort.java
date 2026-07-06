package com.b2b.worker.application.port.out;

import com.b2b.worker.domain.model.DltMessage;
import reactor.core.publisher.Mono;

public interface DeadLetterPublisherPort {

    Mono<Void> publish(DltMessage message);
}
