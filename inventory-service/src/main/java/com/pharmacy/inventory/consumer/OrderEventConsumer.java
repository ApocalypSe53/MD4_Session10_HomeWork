package com.pharmacy.inventory.consumer;

import com.pharmacy.inventory.event.OrderEvent;
import com.pharmacy.inventory.service.InventoryService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventConsumer.class);

    private final InventoryService inventoryService;

    public OrderEventConsumer(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @KafkaListener(
            topics = "${app.kafka.topics.medicine-stock-events}",
            groupId = "${spring.kafka.consumer.group-id}")
    public void onOrderEvent(ConsumerRecord<String, OrderEvent> record) {
        OrderEvent event = record.value();
        log.info("Nhận được sự kiện đơn hàng: key={}, partition={}, offset={}, data={}",
                record.key(), record.partition(), record.offset(), event);

        if (event == null || event.getOrderId() == null || event.getMedicineId() == null || event.getQuantity() <= 0) {
            log.error("Sự kiện không hợp lệ, bỏ qua: {}", event);
            return;
        }

        // Nếu ném exception, offset KHÔNG được commit và DefaultErrorHandler sẽ thử lại
        inventoryService.processOrder(event, record.partition(), record.offset());
    }
}
