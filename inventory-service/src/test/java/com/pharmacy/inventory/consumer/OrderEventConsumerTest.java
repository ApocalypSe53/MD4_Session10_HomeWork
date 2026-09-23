package com.pharmacy.inventory.consumer;

import com.pharmacy.inventory.entity.OrderStatus;
import com.pharmacy.inventory.repository.MedicineOrderRepository;
import com.pharmacy.inventory.repository.MedicineRepository;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(properties = "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}")
@ActiveProfiles("h2")
@EmbeddedKafka(partitions = 3, topics = "medicine-stock-events")
class OrderEventConsumerTest {

    private static final String TOPIC = "medicine-stock-events";

    @Autowired
    private EmbeddedKafkaBroker broker;

    @Autowired
    private MedicineRepository medicineRepository;

    @Autowired
    private MedicineOrderRepository orderRepository;

    private KafkaTemplate<String, String> producer;

    @BeforeEach
    void setUp() {
        Map<String, Object> props = KafkaTestUtils.producerProps(broker);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producer = new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(props));
    }

    @Test
    void orderEventDecreasesStockAndCreatesOrder() {
        String orderId = send("MED-001", 2);

        awaitOrder(orderId);
        assertThat(orderRepository.findById(orderId).orElseThrow().getStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(stockOf("MED-001")).isEqualTo(98);
    }

    @Test
    void duplicateEventIsProcessedOnlyOnce() {
        String orderId = UUID.randomUUID().toString();
        send(orderId, "MED-002", 5);
        send(orderId, "MED-002", 5);
        // Cùng key -> cùng partition -> xử lý sau 2 message trên, nên khi đơn này có trong DB là 2 message trước đã xong
        String marker = send("MED-002", 1);

        awaitOrder(marker);
        assertThat(stockOf("MED-002")).isEqualTo(50 - 5 - 1);
    }

    @Test
    void orderExceedingStockIsRejectedWithoutChangingStock() {
        String orderId = send("MED-004", 50);

        awaitOrder(orderId);
        assertThat(orderRepository.findById(orderId).orElseThrow().getStatus())
                .isEqualTo(OrderStatus.REJECTED_OUT_OF_STOCK);
        assertThat(stockOf("MED-004")).isEqualTo(10);
    }

    @Test
    void unknownMedicineIsRejected() {
        String orderId = send("MED-999", 1);

        awaitOrder(orderId);
        assertThat(orderRepository.findById(orderId).orElseThrow().getStatus())
                .isEqualTo(OrderStatus.REJECTED_MEDICINE_NOT_FOUND);
    }

    private String send(String medicineId, int quantity) {
        return send(UUID.randomUUID().toString(), medicineId, quantity);
    }

    /** Gửi JSON đúng định dạng pharmacy-service tạo ra (không có header kiểu class). */
    private String send(String orderId, String medicineId, int quantity) {
        String json = """
                {"orderId":"%s","medicineId":"%s","quantity":%d,"timestamp":"2026-09-23T05:00:00Z",\
                "customerName":"Test","customerEmail":"test@example.com"}"""
                .formatted(orderId, medicineId, quantity);
        producer.send(TOPIC, medicineId, json).join();
        return orderId;
    }

    private void awaitOrder(String orderId) {
        await().atMost(Duration.ofSeconds(20)).until(() -> orderRepository.existsById(orderId));
    }

    private int stockOf(String medicineId) {
        return medicineRepository.findById(medicineId).orElseThrow().getStock();
    }
}
