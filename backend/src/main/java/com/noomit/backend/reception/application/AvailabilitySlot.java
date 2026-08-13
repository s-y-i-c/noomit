package com.noomit.backend.reception.application;

import java.time.LocalDate;
import java.time.LocalTime;

/** 생성 대상 슬롯. */
public record AvailabilitySlot(long technicianId, LocalDate date, LocalTime startTime, LocalTime endTime) {
}