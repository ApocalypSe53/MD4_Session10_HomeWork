package com.pharmacy.kafka;

/**
 * Danh sách các luồng dữ liệu (topic) của hiệu thuốc và số partition tương ứng.
 */
public enum PharmacyTopic {

    /** Nhập/xuất kho - 3 partition để xử lý song song. */
    MEDICINE_STOCK_EVENTS("medicine-stock-events", 3),

    /** Cập nhật giá - 1 partition để đảm bảo thứ tự tuyệt đối. */
    MEDICINE_PRICE_UPDATES("medicine-price-updates", 1),

    /** Thông báo - 2 partition. */
    PHARMACY_NOTIFICATIONS("pharmacy-notifications", 2);

    private final String topicName;
    private final int partitions;

    PharmacyTopic(String topicName, int partitions) {
        this.topicName = topicName;
        this.partitions = partitions;
    }

    public String topicName() {
        return topicName;
    }

    public int partitions() {
        return partitions;
    }
}
