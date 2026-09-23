package com.pharmacy.service.producer;

import com.pharmacy.service.event.OrderEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}")
@EmbeddedKafka(partitions = 3, topics = "medicine-stock-events")
class OrderEventProducerTest {

    @Autowired
    private OrderEventProducer producer;

    @Test
    void ordersOfSameMedicineGoToSamePartition() throws Exception {
        int firstPartition = sendAndGetPartition("MED-001");

        for (int i = 0; i < 5; i++) {
            assertThat(sendAndGetPartition("MED-001")).isEqualTo(firstPartition);
        }
    }

    private int sendAndGetPartition(String medicineId) throws Exception {
        OrderEvent event = new OrderEvent(UUID.randomUUID().toString(), medicineId, 2, Instant.now());
        var result = producer.send(event).get(10, TimeUnit.SECONDS);
        assertThat(result.getProducerRecord().key()).isEqualTo(medicineId);
        return result.getRecordMetadata().partition();
    }
}
