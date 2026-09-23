package com.pharmacy.inventory.repository;

import com.pharmacy.inventory.entity.MedicineOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicineOrderRepository extends JpaRepository<MedicineOrder, String> {

    List<MedicineOrder> findTop50ByOrderByProcessedAtDesc();
}
