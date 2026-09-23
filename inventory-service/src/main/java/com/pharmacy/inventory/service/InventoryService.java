package com.pharmacy.inventory.service;

import com.pharmacy.inventory.entity.MedicineOrder;
import com.pharmacy.inventory.entity.OrderStatus;
import com.pharmacy.inventory.event.OrderEvent;
import com.pharmacy.inventory.repository.MedicineOrderRepository;
import com.pharmacy.inventory.repository.MedicineRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);

    private final MedicineRepository medicineRepository;
    private final MedicineOrderRepository orderRepository;

    public InventoryService(MedicineRepository medicineRepository, MedicineOrderRepository orderRepository) {
        this.medicineRepository = medicineRepository;
        this.orderRepository = orderRepository;
    }

    /**
     * Thêm mới đơn hàng và trừ tồn kho trong cùng một transaction:
     * hoặc cả hai cùng thành công, hoặc cả hai cùng rollback.
     */
    @Transactional
    public void processOrder(OrderEvent event, int partition, long offset) {
        // Chống xử lý trùng: Kafka đảm bảo at-least-once nên một sự kiện có thể được gửi lại
        // (VD: DB đã commit nhưng consumer chết trước khi commit offset).
        if (orderRepository.existsById(event.getOrderId())) {
            log.warn("Bỏ qua sự kiện trùng lặp, đơn {} đã được xử lý trước đó", event.getOrderId());
            return;
        }

        OrderStatus status;
        if (!medicineRepository.existsById(event.getMedicineId())) {
            status = OrderStatus.REJECTED_MEDICINE_NOT_FOUND;
            log.warn("Không tìm thấy thuốc {} -> từ chối đơn {}", event.getMedicineId(), event.getOrderId());
        } else if (medicineRepository.decreaseStock(event.getMedicineId(), event.getQuantity()) == 0) {
            status = OrderStatus.REJECTED_OUT_OF_STOCK;
            log.warn("Thuốc {} không đủ tồn kho cho {} sản phẩm -> từ chối đơn {}",
                    event.getMedicineId(), event.getQuantity(), event.getOrderId());
        } else {
            status = OrderStatus.COMPLETED;
            int remaining = medicineRepository.findById(event.getMedicineId()).orElseThrow().getStock();
            log.info("Đã trừ kho thuốc {}: -{} -> còn lại {}", event.getMedicineId(), event.getQuantity(), remaining);
        }

        Instant orderTime = event.getTimestamp() != null ? event.getTimestamp() : Instant.now();
        orderRepository.save(new MedicineOrder(event.getOrderId(), event.getMedicineId(),
                event.getQuantity(), status, orderTime, partition, offset));
        log.info("Đã thêm mới đơn hàng {} với trạng thái {}", event.getOrderId(), status);
    }
}
