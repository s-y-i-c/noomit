package com.noomit.backend.reception;

import java.time.Instant;

public record ServiceRequestAssigned(
        Long serviceRequestId,
        Long customerId,
        Long productId,
        Long technicianId,
        Instant assignedAt
) {
}