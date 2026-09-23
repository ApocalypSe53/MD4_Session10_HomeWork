package com.pharmacy.notification.config;

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

    /** Gửi mail lỗi (SMTP tạm thời không phản hồi): thử lại 3 lần, mỗi lần cách 3 giây rồi bỏ qua. */
    @Bean
    public CommonErrorHandler kafkaErrorHandler() {
        return new DefaultErrorHandler(
                (record, ex) -> log.error("Không gửi được thông báo sau khi thử lại: partition={}, offset={}, value={}",
                        record.partition(), record.offset(), record.value(), ex),
                new FixedBackOff(3_000L, 3));
    }
}
