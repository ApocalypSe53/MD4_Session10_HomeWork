# pharmacy-service – Producer gửi sự kiện đơn hàng thuốc

## Chạy

1. Khởi động Kafka (ở thư mục gốc `Session10`): `docker compose up -d`
2. Chạy service: mở `pharmacy-service` bằng IntelliJ → Run `PharmacyServiceApplication`
   (hoặc `mvn spring-boot:run`).
3. Mở http://localhost:8080 → chọn thuốc, nhập số lượng → bấm **Thanh toán**.

## API

```
POST /api/orders/checkout
Content-Type: application/json

{ "medicineId": "MED-001", "quantity": 2 }
```

Phản hồi thành công:

```json
{
  "message": "Thanh toán thành công",
  "orderId": "8b0c...",
  "medicineId": "MED-001",
  "quantity": 2,
  "timestamp": "2026-09-23T05:00:00.123Z",
  "topic": "medicine-stock-events",
  "partition": 1,
  "offset": 0
}
```

Nếu Kafka không hoạt động, API trả về `503` với thông báo "Thanh toán thất bại".

PowerShell:

```powershell
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/orders/checkout `
  -ContentType 'application/json' -Body '{"medicineId":"MED-001","quantity":2}'
```

## Kiểm tra message trên Kafka

```bash
docker exec pharmacy-kafka /opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 --topic medicine-stock-events --from-beginning \
  --property print.key=true --property print.partition=true
```

Gọi API nhiều lần với cùng `medicineId` → tất cả message có cùng `Partition`,
vì Producer dùng `medicineId` làm **message key** (Kafka băm key để chọn partition).

## Test

`mvn test` – chạy `OrderEventProducerTest` với Embedded Kafka, kiểm tra các đơn cùng thuốc luôn vào cùng partition.
