package com.pharmacy.inventory.controller;

import com.pharmacy.inventory.entity.Medicine;
import com.pharmacy.inventory.entity.MedicineOrder;
import com.pharmacy.inventory.repository.MedicineOrderRepository;
import com.pharmacy.inventory.repository.MedicineRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** API chỉ đọc để kiểm tra kết quả Consumer đã cập nhật DB. */
@RestController
@RequestMapping("/api")
public class InventoryController {

    private final MedicineRepository medicineRepository;
    private final MedicineOrderRepository orderRepository;

    public InventoryController(MedicineRepository medicineRepository, MedicineOrderRepository orderRepository) {
        this.medicineRepository = medicineRepository;
        this.orderRepository = orderRepository;
    }

    @GetMapping("/medicines")
    public List<Medicine> medicines() {
        return medicineRepository.findAll();
    }

    @GetMapping("/orders")
    public List<MedicineOrder> orders() {
        return orderRepository.findTop50ByOrderByProcessedAtDesc();
    }
}
