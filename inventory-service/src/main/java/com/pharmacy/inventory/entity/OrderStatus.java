package com.pharmacy.inventory.entity;

public enum OrderStatus {
    /** Đã trừ kho thành công. */
    COMPLETED,
    /** Không đủ tồn kho, không trừ. */
    REJECTED_OUT_OF_STOCK,
    /** Mã thuốc không tồn tại. */
    REJECTED_MEDICINE_NOT_FOUND
}
