package com.pharmacy.service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record SellMedicineRequest(
        @NotBlank(message = "medicineId không được để trống") String medicineId,
        @Min(value = 1, message = "quantity phải >= 1") int quantity,
        String customerName,
        @Email(message = "customerEmail không hợp lệ") String customerEmail) {
}
