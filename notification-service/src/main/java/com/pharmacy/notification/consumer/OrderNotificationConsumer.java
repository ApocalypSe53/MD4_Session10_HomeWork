package com.pharmacy.notification.consumer;

import com.pharmacy.notification.event.OrderEvent;
import com.pharmacy.notification.service.InvoiceEmailService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderNotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderNotificationConsumer.class);

    private final InvoiceEmailService invoiceEmailService;

    public OrderNotificationConsumer(InvoiceEmailService invoiceEmailService) {
        this.invoiceEmailService = invoiceEmailService;
    }

    /**
     * Cùng topic với inventory-service nhưng khác group-id,
     * nên service này nhận MỌI sự kiện một cách độc lập (fan-out).
     */
    @KafkaListener(
            topics = "${app.kafka.topics.medicine-stock-events}",
            groupId = "${spring.kafka.consumer.group-id}")
    public void onOrderEvent(ConsumerRecord<String, OrderEvent> record) {
        OrderEvent event = record.value();
        log.info("[notification-service] Nhận sự kiện: partition={}, offset={}, data={}",
                record.partition(), record.offset(), event);

        if (event == null || event.getOrderId() == null) {
            log.error("Sự kiện không hợp lệ, bỏ qua: {}", event);
            return;
        }

        // Ném exception khi gửi mail lỗi -> DefaultErrorHandler thử lại
        invoiceEmailService.sendInvoice(event);

        log.info("Hóa đơn cho đơn hàng [{}] đã được gửi tới khách hàng", event.getOrderId());
    }
}
