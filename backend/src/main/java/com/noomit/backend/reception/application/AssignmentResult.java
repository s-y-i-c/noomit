package com.noomit.backend.reception.application;

import java.time.LocalDate;
import java.time.LocalTime;

import com.noomit.backend.reception.domain.ServiceRequestStatus;
import com.noomit.backend.reception.domain.TechnicianAvailability;

public record AssignmentResult(
        long id,
        ServiceRequestStatus status,
        String technicianName,
        LocalDate visitDate,
        LocalTime visitStartTime,
        LocalTime visitEndTime) {
    public static AssignmentResult of(
            long id,
            ServiceRequestStatus status,
            String technicianName,
            TechnicianAvailability slot
    ) {
        return new AssignmentResult(
                id,
                status,
                technicianName,
                slot.availableDate(),
                slot.startTime(),
                slot.endTime()
        );
    }
}
