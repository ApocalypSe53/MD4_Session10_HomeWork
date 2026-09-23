package com.pharmacy.inventory.repository;

import com.pharmacy.inventory.entity.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MedicineRepository extends JpaRepository<Medicine, String> {

    /**
     * Trừ tồn kho nguyên tử ngay trong DB. Điều kiện stock >= quantity ngăn tồn kho bị âm.
     *
     * @return số dòng được cập nhật (0 = không đủ hàng hoặc không có thuốc)
     */
    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE medicines SET stock = stock - :quantity "
            + "WHERE id = :medicineId AND stock >= :quantity", nativeQuery = true)
    int decreaseStock(@Param("medicineId") String medicineId, @Param("quantity") int quantity);
}
