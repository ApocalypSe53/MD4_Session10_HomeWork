package com.pharmacy.notification.service;

import com.pharmacy.notification.event.OrderEvent;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
public class InvoiceEmailService {

    private static final Logger log = LoggerFactory.getLogger(InvoiceEmailService.class);
    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy").withZone(ZoneId.of("Asia/Ho_Chi_Minh"));

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final boolean emailEnabled;
    private final String from;

    public InvoiceEmailService(JavaMailSender mailSender,
                               TemplateEngine templateEngine,
                               @Value("${app.notification.email.enabled}") boolean emailEnabled,
                               @Value("${app.notification.email.from}") String from) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.emailEnabled = emailEnabled;
        this.from = from;
    }

    /** Gửi email hóa đơn HTML. Nếu tắt email hoặc đơn không có email khách thì chỉ ghi log. */
    public void sendInvoice(OrderEvent event) {
        if (!emailEnabled || event.getCustomerEmail() == null || event.getCustomerEmail().isBlank()) {
            log.info("Đơn {} không có email khách hàng (hoặc tắt gửi mail) -> chỉ ghi log hóa đơn: {}",
                    event.getOrderId(), event);
            return;
        }

        Context ctx = new Context();
        ctx.setVariable("order", event);
        ctx.setVariable("customerName",
                event.getCustomerName() != null ? event.getCustomerName() : "Quý khách");
        ctx.setVariable("orderTime",
                TIME_FORMAT.format(event.getTimestamp() != null ? event.getTimestamp() : Instant.now()));
        String html = templateEngine.process("invoice-email", ctx);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, StandardCharsets.UTF_8.name());
            helper.setFrom(from);
            helper.setTo(event.getCustomerEmail());
            helper.setSubject("Hóa đơn điện tử - Đơn hàng " + event.getOrderId());
            helper.setText(html, true);
            mailSender.send(message);
            log.info("Đã gửi email hóa đơn đơn {} tới {}", event.getOrderId(), event.getCustomerEmail());
        } catch (MessagingException e) {
            throw new MailSendException("Không tạo được email hóa đơn cho đơn " + event.getOrderId(), e);
        }
    }
}
