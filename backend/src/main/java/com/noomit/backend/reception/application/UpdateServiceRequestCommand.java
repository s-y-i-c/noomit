package com.noomit.backend.reception.application;

public record UpdateServiceRequestCommand(
        long id,
        String customerName,
        String customerPhoneNumber,
        String customerZipCode,
        String customerAddress,
        String customerDetailAddress,
        String customerMemo,
        Long productId,
        Long selectedSubCategoryId,
        String selectedModelName,
        String symptom,
        String remarks,
        long version) {
}
