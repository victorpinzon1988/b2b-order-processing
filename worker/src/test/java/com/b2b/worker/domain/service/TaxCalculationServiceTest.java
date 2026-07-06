package com.b2b.worker.domain.service;

import com.b2b.worker.domain.model.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
public class TaxCalculationServiceTest {

    private final TaxCalculationService service = new TaxCalculationService();

    @Test
    void shouldCalculateTaxesAndTotals(){
        IncomingOrder order = new IncomingOrder(
                "ORD-001",
                "CLI-99821",
                "B2B",
                Instant.parse("2026-02-23T10:45:00Z"),
                List.of(
                        new IncomingOrderItem("PRD-001", 24, new BigDecimal("3500.00")),
                        new IncomingOrderItem("PRD-008", 12, new BigDecimal("8200.00")),
                        new IncomingOrderItem("PRD-010", 5, new BigDecimal("1000.00"))
                )

        );

        Client client = new Client(
                "CLI-99821",
                "Distribuidora Andina S.A",
                "MAYORISTA",
                "RESPONSABLE_IVA",
                "Valle de Cauca"
        );

        List<Product> products = List.of(
                new Product("PRD-001", "Gasesosa 600ml", "GAS-600-PET", "Bebidas", TaxCategory.GRAVADO, "UN"),
                new Product("PRD-008",  "Aceite vegetal 900ml", "ACE-900-BOT", "Alimentos", TaxCategory.REDUCIDO, "UN"),
                new Product("PRD-010", "Panela 500g", "PAN-500-BLO", "Canasta basica exenta", TaxCategory.EXENTO, "UN")
        );

        EnrichedOrder result = service.enrich(order, client, products);

        assertThat(result.orderId()).isEqualTo("ORD-001");
        assertThat(result.status()).isEqualTo("PROCESSED");

        assertThat(result.items()).hasSize(3);

        EnrichedOrderItem first = result.items().getFirst();
        assertThat(first.subtotal()).isEqualByComparingTo("84000.00");
        assertThat(first.taxRate()).isEqualByComparingTo("0.19");
        assertThat(first.taxAmount()).isEqualByComparingTo("15960.00");
        assertThat(first.lineTotal()).isEqualByComparingTo("99960.00");

        EnrichedOrderItem second = result.items().get(1);
        assertThat(second.subtotal()).isEqualByComparingTo("98400.00");
        assertThat(second.taxRate()).isEqualByComparingTo("0.05");
        assertThat(second.taxAmount()).isEqualByComparingTo("4920.00");
        assertThat(second.lineTotal()).isEqualByComparingTo("103320.00");

        EnrichedOrderItem third = result.items().get(2);
        assertThat(third.subtotal()).isEqualByComparingTo("5000.00");
        assertThat(third.taxRate()).isEqualByComparingTo("0.00");
        assertThat(third.taxAmount()).isEqualByComparingTo("0.00");
        assertThat(third.lineTotal()).isEqualByComparingTo("5000.00");

        assertThat(result.summary().subtotal()).isEqualByComparingTo("187400.00");
        assertThat(result.summary().totalTax()).isEqualByComparingTo("20880.00");
        assertThat(result.summary().grandTotal()).isEqualByComparingTo("208280.00");
        assertThat(result.summary().currency()).isEqualTo("COP");
    }
}
