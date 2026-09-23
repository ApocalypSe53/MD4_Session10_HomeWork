package com.pharmacy.service.producer;

import com.pharmacy.service.event.OrderEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class OrderEventProducer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventProducer.class);

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    public OrderEventProducer(KafkaTemplate<String, OrderEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Gửi sự kiện vào topic medicine-stock-events.
     * Key = medicineId: Kafka băm key để chọn partition, nên mọi đơn của cùng một loại thuốc
     * luôn vào cùng một partition và giữ đúng thứ tự.
     */
    public CompletableFuture<SendResult<String, OrderEvent>> send(OrderEvent event) {
        return kafkaTemplate.sendDefault(event.getMedicineId(), event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        var metadata = result.getRecordMetadata();
                        log.info("Đã gửi {} -> topic={}, partition={}, offset={}",
                                event, metadata.topic(), metadata.partition(), metadata.offset());
                    } else {
                        log.error("Gửi sự kiện thất bại: {}", event, ex);
                    }
                });
    }
}
