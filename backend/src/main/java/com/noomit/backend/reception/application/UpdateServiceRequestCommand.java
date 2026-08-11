package com.noomit.backend.reception.application;

public record UpdateServiceRequestCommand(
        long id,
        long customerId,
        Long productId,
        Long selectedSubCategoryId,
        String selectedModelName,
        String symptom,
        String remarks,
        long version) {
}