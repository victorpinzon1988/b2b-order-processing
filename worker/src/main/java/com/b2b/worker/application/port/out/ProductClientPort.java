package com.b2b.worker.application.port.out;

import com.b2b.worker.domain.model.Product;
import reactor.core.publisher.Mono;

public interface ProductClientPort {
    Mono<Product> getProduct(String productId);
}
