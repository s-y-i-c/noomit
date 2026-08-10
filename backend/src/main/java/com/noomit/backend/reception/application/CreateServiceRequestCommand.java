package com.noomit.backend.reception.application;

public record CreateServiceRequestCommand(
        long customerId,
        long productId,
        String symptom,
        String remarks) {
}