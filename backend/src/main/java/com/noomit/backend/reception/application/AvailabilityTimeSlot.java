package com.noomit.backend.reception.application;

import java.time.LocalTime;

public record AvailabilityTimeSlot(LocalTime startTime, LocalTime endTime, boolean available) {
}