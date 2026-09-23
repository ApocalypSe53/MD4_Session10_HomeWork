# inventory-service – Consumer cập nhật kho dược tự động

Lắng nghe topic `medicine-stock-events` → thêm mới đơn hàng vào bảng `orders`
và chạy `UPDATE medicines SET stock = stock - quantity` trong cùng một transaction.

## Chạy

1. Ở thư mục gốc `Session10`: `docker compose up -d` (Kafka + PostgreSQL)
2. Chạy `pharmacy-service` (port 8080) và `inventory-service` (port 8081).
   Bảng `medicines`, `orders` và dữ liệu mẫu MED-001..MED-004 được tạo tự động khi khởi động.
3. Mở http://localhost:8080, bấm **Thanh toán**.

Log của inventory-service:

```
Nhận được sự kiện đơn hàng: key=MED-001, partition=1, offset=0, data=OrderEvent{orderId='8b0c...', medicineId='MED-001', quantity=2, ...}
Đã trừ kho thuốc MED-001: -2 -> còn lại 98
Đã thêm mới đơn hàng 8b0c... với trạng thái COMPLETED
```

**Không có PostgreSQL?** Chạy với DB H2 trong bộ nhớ: `--spring.profiles.active=h2`
(IntelliJ: Run Configuration → Active profiles = `h2`). Xem DB tại http://localhost:8081/h2-console
(JDBC URL `jdbc:h2:mem:inventory_db`, user `sa`, không mật khẩu).

Kiểm tra DB: http://localhost:8081/api/medicines và http://localhost:8081/api/orders, hoặc

```bash
docker exec -it pharmacy-postgres psql -U postgres -d inventory_db -c "SELECT * FROM medicines;" -c "SELECT * FROM orders;"
```

## Đảm bảo nhất quán, không xử lý trùng

| Cơ chế | Tác dụng |
|---|---|
| `group-id: inventory-service-group` | Mọi instance chung một group → mỗi partition chỉ được một consumer đọc, một sự kiện không bị nhiều instance cùng xử lý. |
| Key = `medicineId` (từ producer) | Đơn cùng một thuốc luôn vào cùng partition → xử lý tuần tự, đúng thứ tự. |
| `enable-auto-commit: false`, `ack-mode: record` | Offset chỉ được commit sau khi DB xử lý xong → không mất sự kiện. |
| `order_id` là PRIMARY KEY + kiểm tra `existsById` | Nếu Kafka gửi lại sự kiện (at-least-once), đơn đã xử lý sẽ bị bỏ qua, không trừ kho hai lần. |
| `WHERE stock >= quantity` | Không cho tồn kho âm; đơn không đủ hàng lưu trạng thái `REJECTED_OUT_OF_STOCK`. |
| `DefaultErrorHandler` (3 lần × 2s) + `ErrorHandlingDeserializer` | Lỗi tạm thời được thử lại; message JSON hỏng không làm consumer bị kẹt. |

Test tự động (`mvn test`, dùng Embedded Kafka + H2): trừ kho thành công, sự kiện trùng chỉ xử lý 1 lần,
đơn vượt tồn kho bị từ chối, mã thuốc không tồn tại bị từ chối.

Thử chạy 2 instance (thêm `--server.port=8082`): Kafka chia 3 partition giữa 2 instance, mỗi sự kiện vẫn chỉ được xử lý một lần.
