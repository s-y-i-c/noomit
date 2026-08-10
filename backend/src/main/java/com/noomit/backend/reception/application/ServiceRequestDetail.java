package com.noomit.backend.reception.application;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import com.noomit.backend.reception.domain.ServiceRequestStatus;

public record ServiceRequestDetail(
        long id,
        String customerName,
        String customerPhone,
        String address,
        String detailAddress,
        String modelName,
        String symptom,
        ServiceRequestStatus status,
        String technicianName,
        LocalDate visitDate,
        LocalTime visitStartTime,
        LocalTime visitEndTime,
        String remarks,
        int baseFee,
        Instant requestedAt,
        Instant assignedAt,
        Instant cancelledAt,
        String cancelReason) {
}