package com.pharmacy.notification.consumer;

import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Map;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}")
@EmbeddedKafka(partitions = 3, topics = "medicine-stock-events")
class OrderNotificationConsumerTest {

    @Autowired
    private EmbeddedKafkaBroker broker;

    @MockBean
    private JavaMailSender mailSender;

    @Test
    void sendsInvoiceEmailOnlyWhenCustomerEmailPresent() throws Exception {
        when(mailSender.createMimeMessage()).thenAnswer(inv -> new MimeMessage(Session.getInstance(new Properties())));

        Map<String, Object> props = KafkaTestUtils.producerProps(broker);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        KafkaTemplate<String, String> producer = new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(props));

        // Cùng key -> cùng partition -> xử lý theo thứ tự: đơn không email trước, đơn có email sau
        producer.send("medicine-stock-events", "MED-001",
                "{\"orderId\":\"ORD-NO-EMAIL\",\"medicineId\":\"MED-001\",\"quantity\":1,"
                        + "\"timestamp\":\"2026-09-23T05:00:00Z\"}").join();
        producer.send("medicine-stock-events", "MED-001",
                "{\"orderId\":\"ORD-1\",\"medicineId\":\"MED-001\",\"quantity\":2,"
                        + "\"timestamp\":\"2026-09-23T05:00:00Z\",\"customerName\":\"Nguyễn Văn A\","
                        + "\"customerEmail\":\"khach@example.com\"}").join();

        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender, timeout(20_000).times(1)).send(captor.capture());

        MimeMessage mail = captor.getValue();
        assertThat(mail.getRecipients(Message.RecipientType.TO)[0].toString()).isEqualTo("khach@example.com");
        assertThat(mail.getSubject()).isEqualTo("Hóa đơn điện tử - Đơn hàng ORD-1");
        String html = mail.getContent().toString();
        assertThat(html).contains("ORD-1", "MED-001", "Nguyễn Văn A");
    }
}
