package com.pharmacy.service.event;

import java.time.Instant;

/**
 * Sự kiện đơn hàng thuốc gửi vào topic medicine-stock-events.
 */
public class OrderEvent {

    private String orderId;
    private String medicineId;
    private int quantity;
    private Instant timestamp;
    /** Thông tin khách hàng (tùy chọn) – notification-service dùng để gửi hóa đơn. */
    private String customerName;
    private String customerEmail;

    public OrderEvent() {
    }

    public OrderEvent(String orderId, String medicineId, int quantity, Instant timestamp) {
        this(orderId, medicineId, quantity, timestamp, null, null);
    }

    public OrderEvent(String orderId, String medicineId, int quantity, Instant timestamp,
                      String customerName, String customerEmail) {
        this.orderId = orderId;
        this.medicineId = medicineId;
        this.quantity = quantity;
        this.timestamp = timestamp;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
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
