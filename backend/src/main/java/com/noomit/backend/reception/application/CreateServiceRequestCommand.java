package com.noomit.backend.reception.application;

public record CreateServiceRequestCommand(
        long customerId,
        Long productId,
        Long selectedSubCategoryId,
        String selectedModelName,
        long receptionistId,
        String symptom,
        String remarks) {
}