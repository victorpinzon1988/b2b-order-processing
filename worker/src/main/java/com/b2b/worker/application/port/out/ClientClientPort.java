package com.b2b.worker.application.port.out;

import com.b2b.worker.domain.model.Client;
import reactor.core.publisher.Mono;

public interface ClientClientPort {

    Mono<Client> getClient(String clientId);
}
