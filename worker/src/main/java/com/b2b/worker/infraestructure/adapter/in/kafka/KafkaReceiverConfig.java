package com.b2b.worker.infraestructure.adapter.in.kafka;

import com.b2b.worker.infraestructure.config.KafkaProperties;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;

import java.util.List;
import java.util.Map;

@Configuration
public class KafkaReceiverConfig {

    @Bean
    KafkaReceiver<String, String> kafkaReceiver(KafkaProperties properties){
        Map<String, Object> config = Map.of(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.bootstrapServers(),
            ConsumerConfig.GROUP_ID_CONFIG, properties.consumerGroup(),
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
            ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false
        );

        ReceiverOptions<String, String> options = ReceiverOptions
                .<String, String>create(config)
                .subscription(List.of(properties.ordersTopic()));

        return KafkaReceiver.create(options);
    }
}
