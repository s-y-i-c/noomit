package com.noomit.backend.reception.infrastructure.persistence;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import com.noomit.backend.reception.application.AvailabilityTimeSlot;
import com.noomit.backend.reception.application.TechnicianAvailabilityQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
class TechnicianAvailabilityJpaQueryAdapter implements TechnicianAvailabilityQueryRepository {
    private final TechnicianAvailabilityJpaRepository technicianAvailabilities;

    @Override
    public List<AvailabilityTimeSlot> findByDate(LocalDate date) {
        return technicianAvailabilities.findByDate(date).stream()
                .map(this::toAvailabilityTimeSlot)
                .toList();
    }

    private AvailabilityTimeSlot toAvailabilityTimeSlot(Object[] row) {
        return new AvailabilityTimeSlot(
                (LocalTime) row[0],
                (LocalTime) row[1],
                ((Number) row[2]).intValue() == 1
        );
    }
}