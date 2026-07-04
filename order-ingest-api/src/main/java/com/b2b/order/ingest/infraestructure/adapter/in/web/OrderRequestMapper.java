package com.b2b.order.ingest.infraestructure.adapter.in.web;

import com.b2b.order.ingest.domain.model.Order;
import com.b2b.order.ingest.domain.model.OrderItem;
import com.b2b.order.ingest.infraestructure.adapter.in.web.request.CreateOrderRequest;

public final class OrderRequestMapper {

    private OrderRequestMapper(){}

    public static Order toDomain(CreateOrderRequest request){
        return new Order(
                request.orderId(),
                request.clientId(),
                request.channel(),
                request.createdAt(),
                request.items()
                        .stream()
                        .map(item -> new OrderItem(
                                item.productId(),
                                item.quantity(),
                                item.unitPrice()
                        ))
                        .toList()
        );
    }
}
