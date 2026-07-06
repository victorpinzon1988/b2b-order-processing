package com.b2b.worker.application.port.in;

import reactor.core.publisher.Mono;

public interface ProcessOrderUserCase {
    Mono<Void> process(String payload);
}
