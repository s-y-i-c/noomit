package com.noomit.backend.reception.application;

public record UpdateServiceRequestCommand(
        long id,
        long customerId,
        long productId,
        String symptom,
        String remarks) {
}