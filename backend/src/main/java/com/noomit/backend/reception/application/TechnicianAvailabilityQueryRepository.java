package com.noomit.backend.reception.application;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import com.noomit.backend.reception.domain.TechnicianAvailability;

public interface TechnicianAvailabilityQueryRepository {
    List<AvailabilityTimeSlot> findByDate(LocalDate date);
    
    List<TechnicianAvailability> findAvailableSlots(LocalDate date, LocalTime startTime, LocalTime endTime);
    
    List<MyAvailabilitySlot> findByTechnicianAndDate(long technicianId, LocalDate date);
}