package com.pharmacy.inventory.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConsumerConfig {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerConfig.class);

    /**
     * Khi xử lý lỗi (VD: DB tạm thời mất kết nối): thử lại 3 lần, mỗi lần cách 2 giây.
     * Hết số lần thử thì ghi log và bỏ qua message để consumer không bị kẹt mãi ở một offset.
     * Lỗi deserialize JSON không được thử lại (Spring mặc định coi là lỗi không thể phục hồi).
     * Spring Boot tự gắn bean này vào listener container factory.
     */
    @Bean
    public CommonErrorHandler kafkaErrorHandler() {
        return new DefaultErrorHandler(
                (record, ex) -> log.error("Bỏ qua message sau khi thử lại thất bại: topic={}, partition={}, offset={}, value={}",
                        record.topic(), record.partition(), record.offset(), record.value(), ex),
                new FixedBackOff(2_000L, 3));
    }
}
