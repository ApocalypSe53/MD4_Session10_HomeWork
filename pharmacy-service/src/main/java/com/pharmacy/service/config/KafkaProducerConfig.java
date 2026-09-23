package com.pharmacy.service.config;

import com.pharmacy.service.event.OrderEvent;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${app.kafka.topics.medicine-stock-events}")
    private String stockEventsTopic;

    @Bean
    public ProducerFactory<String, OrderEvent> orderEventProducerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        // Key = medicineId (String), Value = OrderEvent dạng JSON
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        // Không gắn header __TypeId__ để consumer ở service khác không phụ thuộc tên class Java
        props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);

        // Đảm bảo độ tin cậy: chờ tất cả replica xác nhận, bật idempotence để không bị trùng khi retry
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);

        // Không để request thanh toán bị treo quá lâu khi broker không phản hồi
        props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 5_000);
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 5_000);
        props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 10_000);

        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, OrderEvent> orderEventKafkaTemplate(
            ProducerFactory<String, OrderEvent> orderEventProducerFactory) {
        KafkaTemplate<String, OrderEvent> template = new KafkaTemplate<>(orderEventProducerFactory);
        template.setDefaultTopic(stockEventsTopic);
        return template;
    }

    /** Đảm bảo topic tồn tại với 3 partition (nếu đã có thì Spring bỏ qua). */
    @Bean
    public NewTopic medicineStockEventsTopic() {
        return TopicBuilder.name(stockEventsTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
