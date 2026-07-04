package com.b2b.order.ingest.infraestructure.adapter.in.web;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OrderIngestApiKafkaTest {

    private static final String TOPIC = "orders-topic";

    @Container
    static KafkaContainer kafka = new KafkaContainer(
            DockerImageName.parse("apache/kafka")
    );

    @LocalServerPort
    int port;

    @Autowired
    WebTestClient webTestClient;

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry){
        registry.add("app.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("app.kafka.orders-topic", () -> TOPIC);
    }

    @BeforeAll
    static void createTopic() throws Exception{
        try(AdminClient adminClient = AdminClient.create(
                Map.of("bootstrap.servers", kafka.getBootstrapServers())
        )){
            adminClient.createTopics(List.of(new NewTopic(TOPIC, 1, (short) 1))).all().get();
        }
    }

    @Test
    void shouldPublishOrderToKafka(){
        String orderId = "ORD-" + UUID.randomUUID();

        webTestClient.post()
                .uri("/orders")
                .bodyValue("""
                        {
                          "orderId": "%s",
                          "clientId": "CLI-99821",
                          "channel": "B2B",
                          "createdAt": "2024-09-12T10:45:00Z",
                          "items": [
                            { "productId": "PRD-001", "quantity": 24, "unitPrice": 3500.00 }
                          ]
                        }
                        """.formatted(orderId))
                .header("Content-Type", "application/json")
                .exchange()
                .expectStatus().isAccepted();

        try(KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProperties())){
            consumer.subscribe(List.of(TOPIC));

            var records = consumer.poll(Duration.ofSeconds(10));

            assertThat(records).isNotEmpty();

            var record = records.iterator().next();

            assertThat(record.key()).isEqualTo(orderId);
            assertThat(record.value()).contains(orderId);
            assertThat(record.value()).contains("CLI-99821");
            assertThat(record.value()).contains("PRD-001");

        }
    }

    private Map<String, Object> consumerProperties(){
        return Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers(),
                ConsumerConfig.GROUP_ID_CONFIG, "test-consumer-" + UUID.randomUUID(),
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class
        );
    }

}
