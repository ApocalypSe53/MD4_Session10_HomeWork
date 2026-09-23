package com.pharmacy.inventory.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "orders")
public class MedicineOrder {

    @Id
    @Column(name = "order_id")
    private String orderId;

    @Column(name = "medicine_id", nullable = false)
    private String medicineId;

    @Column(nullable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(name = "order_time", nullable = false)
    private Instant orderTime;

    @Column(name = "processed_at", nullable = false)
    private Instant processedAt;

    @Column(name = "kafka_partition", nullable = false)
    private int kafkaPartition;

    @Column(name = "kafka_offset", nullable = false)
    private long kafkaOffset;

    protected MedicineOrder() {
    }

    public MedicineOrder(String orderId, String medicineId, int quantity, OrderStatus status,
                         Instant orderTime, int kafkaPartition, long kafkaOffset) {
        this.orderId = orderId;
        this.medicineId = medicineId;
        this.quantity = quantity;
        this.status = status;
        this.orderTime = orderTime;
        this.processedAt = Instant.now();
        this.kafkaPartition = kafkaPartition;
        this.kafkaOffset = kafkaOffset;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getMedicineId() {
        return medicineId;
    }

    public int getQuantity() {
        return quantity;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Instant getOrderTime() {
        return orderTime;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public int getKafkaPartition() {
        return kafkaPartition;
    }

    public long getKafkaOffset() {
        return kafkaOffset;
    }
}
