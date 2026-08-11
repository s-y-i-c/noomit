package com.noomit.backend.reception.application;

public record CreateServiceRequestCommand(
        String customerName,
        String customerPhoneNumber,
        String customerZipCode,
        String customerAddress,
        String customerDetailAddress,
        String customerMemo,
        Long productId,
        Long selectedSubCategoryId,
        String selectedModelName,
        long receptionistId,
        String symptom,
        String remarks) {
}
