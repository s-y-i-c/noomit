package com.noomit.backend.reception.application;

import java.time.LocalTime;
import com.noomit.backend.reception.domain.TechnicianAvailabilityStatus;

public record MyAvailabilitySlot(
        long slotId,
        LocalTime startTime,
        LocalTime endTime,
        TechnicianAvailabilityStatus status,
        boolean isAssigned) {
}