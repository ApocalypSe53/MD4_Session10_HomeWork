package com.pharmacy.notification.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;

/**
 * Sự kiện đơn hàng nhận từ pharmacy-service (cùng cấu trúc JSON với bên producer).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderEvent {

    private String orderId;
    private String medicineId;
    private int quantity;
    private Instant timestamp;
    private String customerName;
    private String customerEmail;

    public OrderEvent() {
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

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    @Override
    public String toString() {
        return "OrderEvent{orderId='" + orderId + "', medicineId='" + medicineId
                + "', quantity=" + quantity + ", timestamp=" + timestamp
                + ", customerName='" + customerName + "', customerEmail='" + customerEmail + "'}";
    }
}
