package com.pharmacy.service.controller;

import com.pharmacy.service.dto.CheckoutResponse;
import com.pharmacy.service.dto.SellMedicineRequest;
import com.pharmacy.service.event.OrderEvent;
import com.pharmacy.service.producer.OrderEventProducer;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderEventProducer producer;

    public OrderController(OrderEventProducer producer) {
        this.producer = producer;
    }

    /** API bán thuốc – được gọi khi nhân viên bấm "Thanh toán". */
    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@Valid @RequestBody SellMedicineRequest request)
            throws InterruptedException {
        OrderEvent event = new OrderEvent(
                UUID.randomUUID().toString(),
                request.medicineId(),
                request.quantity(),
                Instant.now(),
                blankToNull(request.customerName()),
                blankToNull(request.customerEmail()));

        try {
            // Chờ broker xác nhận để phản hồi thanh toán thành công kèm partition/offset
            var metadata = producer.send(event).get(15, TimeUnit.SECONDS).getRecordMetadata();
            return ResponseEntity.ok(new CheckoutResponse(
                    "Thanh toán thành công",
                    event.getOrderId(),
                    event.getMedicineId(),
                    event.getQuantity(),
                    event.getTimestamp(),
                    metadata.topic(),
                    metadata.partition(),
                    metadata.offset()));
        } catch (ExecutionException | TimeoutException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("message", "Thanh toán thất bại: không gửi được sự kiện lên Kafka",
                            "orderId", event.getOrderId()));
        }
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException e) {
        String error = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getDefaultMessage())
                .findFirst()
                .orElse("Dữ liệu không hợp lệ");
        return ResponseEntity.badRequest().body(Map.of("message", error));
    }
}
