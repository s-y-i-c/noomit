package com.noomit.backend.reception.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

public record ServiceRequest(
        Long id,
        long customerId,
        long productId,
        String symptom,
        String remarks,
        int baseFee,
        Long reservedSlotId,
        Long technicianId,
        LocalDate visitDate,
        LocalTime visitStartTime,
        LocalTime visitEndTime,
        ServiceRequestStatus status,
        Instant requestedAt,
        Instant assignedAt,
        Instant cancelledAt,
        String cancelReason,
        long version,
        Instant createdAt,
        Instant updatedAt) {
}