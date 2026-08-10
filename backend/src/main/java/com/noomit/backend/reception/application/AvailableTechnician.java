package com.noomit.backend.reception.application;

import java.time.LocalTime;

public record AvailableTechnician(long technicianId, String technicianName, long slotId, LocalTime startTime, LocalTime endTime) {
}