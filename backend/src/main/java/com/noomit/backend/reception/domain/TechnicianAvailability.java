package com.noomit.backend.reception.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

public record TechnicianAvailability(
        Long id,
        long technicianId,
        LocalDate availableDate,
        LocalTime startTime,
        LocalTime endTime,
        TechnicianAvailabilityStatus status,
        Instant createdAt,
        Instant updatedAt) {

    public boolean isOwnedBy(long technicianId) {
        return this.technicianId == technicianId;
    }
}