package com.noomit.backend.reception.application;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TechnicianAvailabilityQueryService {
    private final TechnicianAvailabilityQueryRepository technicianQueryRepository;

    public List<AvailabilityTimeSlot> getByDate(LocalDate date) {
        return technicianQueryRepository.findByDate(date);
    }
}