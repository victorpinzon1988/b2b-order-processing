package com.b2b.worker;

import com.b2b.worker.infraestructure.adapter.out.mongo.EnrichedOrderDocument;
import com.b2b.worker.infraestructure.adapter.out.mongo.ReactiveEnrichedOrderMongoRepository;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;
import reactor.kafka.sender.SenderRecord;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

import org.awaitility.Awaitility;
import java.util.concurrent.TimeUnit;


@Testcontainers
@SpringBootTest
public class OrderWorkerE2ETest {

    private static final String ORDERS_TOPIC = "orders-topic";
    private static final String DLT_TOPIC = "orders-dlt";

    @Container
    static KafkaContainer kafka = new KafkaContainer(
            DockerImageName.parse("apache/kafka")
    );

    @Container
    static MongoDBContainer mongo = new MongoDBContainer(
            DockerImageName.parse("mongo:7.0")
    );

    static WireMockServer productsApi;
    static WireMockServer clientsApi;

    @Autowired
    ReactiveEnrichedOrderMongoRepository mongoRepository;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry){
        registry.add("app.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("app.kafka.orders-topic", () -> ORDERS_TOPIC);
        registry.add("app.kafka.dlt-topic", () -> DLT_TOPIC);
        registry.add("app.kafka.consumer-group", () -> "order-worker-e2e-test");

        registry.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);

        registry.add("app.external-apis.products-base-url", () -> productsApi.baseUrl());
        registry.add("app.external-apis.clients-base-url", () -> clientsApi.baseUrl());
    }

    @BeforeAll
    static void beforeAll() throws Exception{
        productsApi = new WireMockServer(0);
        clientsApi = new WireMockServer(0);

        productsApi.start();
        clientsApi.start();

        try(AdminClient adminClient = AdminClient.create(
                Map.of("bootstrap.servers", kafka.getBootstrapServers())
        )){
            adminClient.createTopics(List.of(
                new NewTopic(ORDERS_TOPIC, 1, (short) 1),
                new NewTopic(DLT_TOPIC, 1, (short) 1)
            )).all().get();
        }

        productsApi.stubFor(get(urlEqualTo("/products/PRD-001"))
                .willReturn(okJson("""
                    {
                      "productId": "PRD-001",
                      "name": "Gaseosa 600ml",
                      "sku": "GAS-600-PET",
                      "category": "Bebidas azucaradas",
                      "taxCategory": "GRAVADO",
                      "unitOfMeasure": "UN"
                    }
                    """)));
        clientsApi.stubFor(get(urlEqualTo("/clients/CLI-99821"))
                .willReturn(okJson("""
                        {
                          "clientId": "CLI-99821",
                          "name": "Distribuidora Andina S.A.S",
                          "segment": "MAYORISTA",
                          "taxRegime": "RESPONSABLE_IVA",
                          "region": "Valle del Cauca"
                        }
                        """)));
    }

    @AfterAll
    static void afterAll(){
        productsApi.stop();
        clientsApi.stop();
    }

    @Test
    void shouldConsumeOrderFromKafkaAndPersistEnrichedOrderInMongo(){
        String payload = """
                 {
                  "orderId": "ORD-E2E-001",
                  "clientId": "CLI-99821",
                  "channel": "B2B",
                  "createdAt": "2026-02-23T10:45:00Z",
                  "items": [
                    { "productId": "PRD-001", "quantity": 24, "unitPrice": 3500.00 }
                  ]
                }
                """;

        publishOrder("ORD-E2E-001", payload);

        Awaitility.await()
                .atMost(20, TimeUnit.SECONDS)
                .pollInterval(500, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    EnrichedOrderDocument order = mongoRepository
                            .findAll()
                            .filter(saved -> "ORD-E2E-001".equals(saved.orderId()))
                            .next()
                            .block();

                    assertThat(order).isNotNull();
                    assertThat(order.status()).isEqualTo("PROCESSED");
                    assertThat(order.client().clientId()).isEqualTo("CLI-99821");
                    assertThat(order.items()).hasSize(1);
                    assertThat(order.items().getFirst().productId()).isEqualTo("PRD-001");
                    assertThat(order.items().getFirst().taxAmount()).isEqualByComparingTo("15960.00");
                    assertThat(order.summary().grandTotal()).isEqualByComparingTo("99960.00");
                });

    }

    @Test
    void shouldPublishInvalidOrderToDlt(){
        String payload = """
                 {
                  "orderId": "ORD-E2E-DLT-001",
                  "clientId": "CLI-99821",
                  "channel": "B2B",
                  "createdAt": "2026-02-23T10:45:00Z",
                  "items": []
                }
                """;

        publishOrder("ORD-E2E-DLT-001", payload);

        AtomicReference<String> dltPayload = new AtomicReference<>();

        try(KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerConfig("dlt-test-group"))){
            consumer.subscribe(List.of(DLT_TOPIC));

            Awaitility.await()
                    .atMost(20, TimeUnit.SECONDS)
                    .pollInterval(500, TimeUnit.MILLISECONDS)
                    .untilAsserted(() -> {
                        consumer.poll(Duration.ofMillis(500))
                                .forEach(record -> {
                                    if(record.value().contains("ORD-E2E-DLT-001")){
                                        dltPayload.set(record.value());
                                    }
                                });

                        assertThat(dltPayload.get()).isNotNull();
                        assertThat(dltPayload.get()).contains("ORD-E2E-DLT-001");
                        assertThat(dltPayload.get()).contains("Invalid order payload");
                        assertThat(dltPayload.get()).contains("IllegalArgumentException");
                    });
        }

        StepVerifier.create(
                        mongoRepository.findAll()
                                .filter(saved -> "ORD-E2E-DLT-001".equals(saved.orderId()))
                                .count()
                )
                .expectNext(0L)
                .verifyComplete();
    }

    @Test
    void shouldNotDuplicateAlreadyProcessedOrder(){
        String payload = """
                 {
                  "orderId": "ORD-E2E-IDEMPOTENT-001",
                  "clientId": "CLI-99821",
                  "channel": "B2B",
                  "createdAt": "2026-02-23T10:45:00Z",
                  "items": [
                    { "productId": "PRD-001", "quantity": 24, "unitPrice": 3500.00 }
                  ]
                }
                """;

        publishOrder("ORD-E2E-IDEMPOTENT-001", payload);

        Awaitility.await()
                .atMost(20, TimeUnit.SECONDS)
                .pollInterval(500, TimeUnit.MILLISECONDS)
                .untilAsserted(() ->
                        StepVerifier.create(countOrders("ORD-E2E-IDEMPOTENT-001"))
                                .expectNext(1L)
                                .verifyComplete()
                );

        publishOrder("ORD-E2E-IDEMPOTENT-001", payload);

        Awaitility.await()
                .atMost(5, TimeUnit.SECONDS)
                .pollInterval(500, TimeUnit.MILLISECONDS)
                .untilAsserted(() ->
                        StepVerifier.create(countOrders("ORD-E2E-IDEMPOTENT-001"))
                                .expectNext(1L)
                                .verifyComplete()
                );
    }

    private void publishOrder(String orderId, String payload){
        Map<String, Object> config = Map.of(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers(),
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
            ProducerConfig.ACKS_CONFIG, "all"
        );

        KafkaSender<String, String> sender = KafkaSender.create(SenderOptions.create(config));

        StepVerifier.create(
                sender.send(Mono.just(SenderRecord.create(
                        ORDERS_TOPIC,
                        null,
                        null,
                        orderId,
                        payload,
                        orderId
                ))).then()
            ).verifyComplete();

        sender.close();
    }

    private Mono<Long> countOrders(String orderId){
        return mongoRepository.findAll()
                .filter(saved -> orderId.equals(saved.orderId()))
                .count();
    }

    private Map<String, Object> consumerConfig(String groupId){
        return Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers(),
                ConsumerConfig.GROUP_ID_CONFIG, groupId,
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class
        );
    }

}
