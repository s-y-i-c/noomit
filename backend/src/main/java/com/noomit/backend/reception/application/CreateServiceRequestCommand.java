package com.noomit.backend.reception.application;

public record CreateServiceRequestCommand(
        long customerId,
        long productId,
        long receptionistId,
        String symptom,
        String remarks) {
}