package com.noomit.backend.reception.application;

import java.time.LocalDate;
import java.util.List;

public interface TechnicianAvailabilityQueryRepository {
    List<AvailabilityTimeSlot> findByDate(LocalDate date);
}