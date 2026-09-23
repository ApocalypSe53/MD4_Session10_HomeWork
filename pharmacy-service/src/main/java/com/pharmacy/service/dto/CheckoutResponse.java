package com.pharmacy.service.dto;

import java.time.Instant;

public record CheckoutResponse(
        String message,
        String orderId,
        String medicineId,
        int quantity,
        Instant timestamp,
        String topic,
        int partition,
        long offset) {
}
