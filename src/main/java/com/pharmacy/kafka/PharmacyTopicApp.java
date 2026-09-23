package com.pharmacy.kafka;

/**
 * Chương trình thiết lập topic cho hiệu thuốc.
 * Tham số (tùy chọn): địa chỉ bootstrap server, mặc định localhost:9092.
 */
public class PharmacyTopicApp {

    public static void main(String[] args) throws Exception {
        String bootstrap = args.length > 0 ? args[0] : "localhost:9092";

        try (TopicManager manager = new TopicManager(bootstrap)) {
            System.out.println("===== 1. THÔNG TIN CLUSTER =====");
            manager.printClusterInfo();

            System.out.println("\n===== 2. TẠO TOPIC =====");
            manager.createTopics();

            System.out.println("\n===== 3. DANH SÁCH TOPIC =====");
            manager.listTopics().stream().sorted().forEach(name -> System.out.println("  - " + name));

            System.out.println("\n===== 4. CHI TIẾT PARTITION =====");
            manager.describeTopics();
        }
    }
}
