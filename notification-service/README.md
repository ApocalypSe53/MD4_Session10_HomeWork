# notification-service – Gửi hóa đơn cho khách hàng (Fan-out)

Lắng nghe **cùng topic** `medicine-stock-events` với inventory-service nhưng dùng **group-id khác**:

| Service | group-id | Việc làm |
|---|---|---|
| inventory-service | `inventory-service-group` | Thêm đơn hàng, trừ tồn kho |
| notification-service | `notification-service-group` | Gửi hóa đơn cho khách |

Kafka lưu offset riêng cho từng consumer group, nên **mỗi group nhận một bản đầy đủ của mọi sự kiện**:
một đơn hàng gửi lên Kafka kích hoạt đồng thời cả trừ kho và gửi thông báo, hai service hoạt động độc lập
(một service chết không ảnh hưởng service còn lại; khi chạy lại, nó đọc tiếp từ offset đã commit).

> Nếu đặt chung group-id, Kafka sẽ **chia** partition giữa hai service → mỗi sự kiện chỉ một bên nhận được.

## Chạy

1. `docker compose up -d` ở thư mục gốc (Kafka + PostgreSQL + Mailpit)
2. Chạy `pharmacy-service`, `inventory-service`, `notification-service` (không mở cổng web)
3. Mở http://localhost:8080, nhập email khách → **Thanh toán**
4. Xem email hóa đơn tại Mailpit: http://localhost:8025

Log notification-service:

```
[notification-service] Nhận sự kiện: partition=1, offset=3, data=OrderEvent{orderId='8b0c...', ...}
Đã gửi email hóa đơn đơn 8b0c... tới khachhang@example.com
Hóa đơn cho đơn hàng [8b0c...] đã được gửi tới khách hàng
```

Đơn không nhập email → chỉ in log thông báo, không gửi mail.

## Gửi email thật qua Gmail

Tạo [App Password](https://myaccount.google.com/apppasswords) rồi đặt biến môi trường trước khi chạy:

```powershell
$env:MAIL_HOST="smtp.gmail.com"; $env:MAIL_PORT="587"
$env:MAIL_USERNAME="you@gmail.com"; $env:MAIL_PASSWORD="app-password-16-ky-tu"
$env:MAIL_SMTP_AUTH="true"; $env:MAIL_STARTTLS="true"; $env:MAIL_FROM="you@gmail.com"
```

Không commit mật khẩu vào code.
