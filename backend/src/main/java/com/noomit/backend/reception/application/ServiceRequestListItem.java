package com.noomit.backend.reception.application;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import com.noomit.backend.reception.domain.ServiceRequestStatus;

public record ServiceRequestListItem(
        long id,
        String requestNumber,
        String customerName,
        String customerPhone,
        String modelName,
        String symptom,
        ServiceRequestStatus status,
        String receptionistName,
        String technicianName,
        LocalDate visitDate,
        LocalTime visitStartTime,
        LocalTime visitEndTime,
        Instant requestedAt) {
}