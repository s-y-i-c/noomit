package com.noomit.backend.reception.infrastructure.persistence;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import com.noomit.backend.reception.application.AvailabilityTimeSlot;
import com.noomit.backend.reception.application.MyAvailabilitySlot;
import com.noomit.backend.reception.application.TechnicianAvailabilityQueryRepository;
import com.noomit.backend.reception.domain.TechnicianAvailability;
import com.noomit.backend.reception.domain.TechnicianAvailabilityStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
class TechnicianAvailabilityJpaQueryAdapter implements TechnicianAvailabilityQueryRepository {
    private final TechnicianAvailabilityJpaRepository availabilityJpaRepository;

    @Override
    public List<AvailabilityTimeSlot> findByDate(LocalDate date) {
        return availabilityJpaRepository.findByDate(date).stream()
                .map(this::toAvailabilityTimeSlot)
                .toList();
    }

    @Override
    public List<TechnicianAvailability> findAvailableSlots(LocalDate date, LocalTime startTime, LocalTime endTime) {
        return availabilityJpaRepository
                .findByAvailableDateAndStartTimeAndEndTimeAndStatus(date, startTime, endTime, TechnicianAvailabilityStatus.AVAILABLE)
                .stream()
                .map(TechnicianAvailabilityEntity::toDomain)
                .toList();
    }

    @Override
    public List<MyAvailabilitySlot> findByTechnicianAndDate(long technicianId, LocalDate date) {
        return availabilityJpaRepository.findByTechnicianAndDate(technicianId, date);
    }

    private AvailabilityTimeSlot toAvailabilityTimeSlot(Object[] row) {
        return new AvailabilityTimeSlot(
                (LocalTime) row[0],
                (LocalTime) row[1],
                ((Number) row[2]).intValue() == 1
        );
    }
}