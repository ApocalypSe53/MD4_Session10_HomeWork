# MD4 Session 10 – Kafka cho hệ thống hiệu thuốc

| Bài | Thư mục | Nội dung |
|---|---|---|
| 1 | `/` (gốc: `pom.xml`, `src/`) + `docker-compose.yml` | Thiết lập hạ tầng Kafka (KRaft), tạo & kiểm tra 3 topic bằng Java AdminClient |
| 2 | [`pharmacy-service`](pharmacy-service) | Producer: API/nút "Thanh toán" gửi `OrderEvent` (JSON, key = `medicineId`) |
| 3 | [`inventory-service`](inventory-service) | Consumer: `@KafkaListener` thêm đơn hàng + trừ tồn kho PostgreSQL |
| 4 | [`notification-service`](notification-service) | Consumer thứ hai (group-id khác) – Fan-out: gửi hóa đơn email |

```
                                  ┌─ group: inventory-service-group ──► inventory-service ──► PostgreSQL
pharmacy-service ──► medicine-stock-events (3 partitions)
   (Producer)                     └─ group: notification-service-group ► notification-service ──► Email
```

## Hạ tầng

```bash
docker compose up -d
```

| Thành phần | Địa chỉ |
|---|---|
| Kafka broker (KRaft) | `localhost:9092` |
| PostgreSQL | `localhost:5432` – db `inventory_db`, user/pass `postgres/postgres` |
| Mailpit (SMTP test) | SMTP `localhost:1025`, web http://localhost:8025 |

Mỗi service là một project Maven độc lập – mở từng thư mục bằng IntelliJ để chạy.

> **Windows 10/11 Home:** Docker Desktop cần WSL2. Nếu `docker version` báo lỗi `500 Internal Server Error`,
> mở PowerShell **Run as Administrator** chạy `wsl --install`, khởi động lại máy rồi mở lại Docker Desktop.

## Test tự động

```bash
mvn -f pharmacy-service/pom.xml test       # key medicineId -> cùng partition
mvn -f inventory-service/pom.xml test      # trừ kho, chống trùng, hết hàng, sai mã thuốc
mvn -f notification-service/pom.xml test   # gửi email hóa đơn đúng người nhận/nội dung
```

Test dùng Embedded Kafka (+ H2 cho inventory), không cần Docker.

---

## Bài 1 – Thiết lập hạ tầng và Quản lý Topic

| Topic                    | Partitions | Mục đích                          |
|--------------------------|-----------:|-----------------------------------|
| `medicine-stock-events`  | 3          | Nhập/xuất kho (xử lý song song)   |
| `medicine-price-updates` | 1          | Cập nhật giá (đảm bảo thứ tự)     |
| `pharmacy-notifications` | 2          | Thông báo                         |

Chạy `PharmacyTopicApp` (hoặc `mvn compile exec:java` ở thư mục gốc): in thông tin cluster → tạo topic còn thiếu
→ liệt kê topic → in chi tiết partition (Leader, Replicas, ISR) và kiểm tra số partition đúng yêu cầu.

Kiểm tra bằng CLI:

```bash
docker exec pharmacy-kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --list
docker exec pharmacy-kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --describe --topic medicine-stock-events
docker exec pharmacy-kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --describe --topic medicine-price-updates
docker exec pharmacy-kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --describe --topic pharmacy-notifications
```

## Bài 2, 3, 4

Xem README trong từng thư mục service. Demo toàn luồng:

1. `docker compose up -d`
2. Chạy `pharmacy-service` (8080), `inventory-service` (8081), `notification-service`
3. Mở http://localhost:8080 → chọn thuốc, nhập email → **Thanh toán**
4. Kết quả:
   - Trang web: "Thanh toán thành công" + partition/offset
   - inventory-service log: nhận sự kiện → trừ kho → thêm đơn hàng; xem http://localhost:8081/api/medicines
   - notification-service log: `Hóa đơn cho đơn hàng [orderId] đã được gửi tới khách hàng`; xem mail tại http://localhost:8025
