package com.b2b.worker.domain.service;

import com.b2b.worker.domain.model.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;

public class TaxCalculationService {

    public EnrichedOrder enrich(IncomingOrder order, Client client, List<Product> products){
        List<EnrichedOrderItem> items = order.items()
                .stream()
                .map(item -> enrichItem(item, findProduct(products, item.productId())))
                .toList();

        BigDecimal subtotal = sumValues(items.stream().map(EnrichedOrderItem::subtotal).toList());
        BigDecimal totalTax = sumValues(items.stream().map(EnrichedOrderItem::taxAmount).toList());
        BigDecimal grandTotal = sumValues(items.stream().map(EnrichedOrderItem::lineTotal).toList());

        return new EnrichedOrder(
                order.orderId(),
                "PROCESSED",
                client,
                items,
                new OrderSummary(subtotal, totalTax, grandTotal, "COP"),
                Instant.now()
        );
    }

    private EnrichedOrderItem enrichItem(IncomingOrderItem item, Product product){
        BigDecimal unitPrice = monetaryValue(item.unitPrice());
        BigDecimal subtotal = monetaryValue(unitPrice.multiply(BigDecimal.valueOf(item.quantity())));
        BigDecimal taxRate = taxRate(product.taxCategory());
        BigDecimal taxAmount = monetaryValue(subtotal.multiply(taxRate));
        BigDecimal lineTotal = monetaryValue(subtotal.add(taxAmount));

        return new EnrichedOrderItem(
                product.productId(),
                product.name(),
                product.sku(),
                product.taxCategory(),
                item.quantity(),
                unitPrice,
                subtotal,
                taxRate,
                taxAmount,
                lineTotal
        );
    }

    private BigDecimal taxRate(TaxCategory taxCategory){
        return switch (taxCategory){
            case GRAVADO -> new BigDecimal("0.19");
            case REDUCIDO -> new BigDecimal("0.05");
            case EXENTO -> new BigDecimal("0.00");
        };
    }

    private BigDecimal monetaryValue(BigDecimal value){
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private Product findProduct(List<Product> products, String productId){
        return products.stream()
                .filter(product -> product.productId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Product " + productId + " not found"));
    }

    private BigDecimal sumValues(List<BigDecimal> values){
        return monetaryValue(values.stream().reduce(BigDecimal.ZERO, BigDecimal::add));
    }
}
