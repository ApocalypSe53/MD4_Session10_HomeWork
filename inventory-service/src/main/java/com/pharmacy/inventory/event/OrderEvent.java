package com.pharmacy.inventory.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;

/**
 * Sự kiện đơn hàng nhận từ pharmacy-service (cùng cấu trúc JSON với bên producer).
 * Bỏ qua các trường không dùng tới (VD: customerEmail) để producer thêm trường không làm hỏng consumer.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderEvent {

    private String orderId;
    private String medicineId;
    private int quantity;
    private Instant timestamp;

    public OrderEvent() {
    }

    public OrderEvent(String orderId, String medicineId, int quantity, Instant timestamp) {
        this.orderId = orderId;
        this.medicineId = medicineId;
        this.quantity = quantity;
        this.timestamp = timestamp;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(String medicineId) {
        this.medicineId = medicineId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "OrderEvent{orderId='" + orderId + "', medicineId='" + medicineId
                + "', quantity=" + quantity + ", timestamp=" + timestamp + "}";
    }
}
