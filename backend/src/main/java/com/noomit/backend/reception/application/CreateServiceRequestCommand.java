package com.noomit.backend.reception.application;

import java.time.Instant;

public record CreateServiceRequestCommand(
        long customerId,
        long productId,
        String symptom,
        String remarks,
        Instant requestedAt) {
}