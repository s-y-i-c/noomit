package com.noomit.backend.reception.application;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalTime;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import com.noomit.backend.reception.domain.TechnicianAvailability;
import com.noomit.backend.user.UserDirectory;
import com.noomit.backend.user.UserRef;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TechnicianAvailabilityQueryService {
    private final TechnicianAvailabilityQueryRepository technicianQueryRepository;
    private final UserDirectory userDirectory;

    public List<AvailabilityTimeSlot> getByDate(LocalDate date) {
        return technicianQueryRepository.findByDate(date);
    }

    public List<MyAvailabilitySlot> getMyAvailabilitySlots(long technicianId, LocalDate date) {
        return technicianQueryRepository.findByTechnicianAndDate(technicianId, date);
    }

    public List<AvailableTechnician> getAvailableTechnicians(LocalDate date, LocalTime startTime, LocalTime endTime) {
        List<TechnicianAvailability> slots = technicianQueryRepository.findAvailableSlots(date, startTime, endTime);

        if (slots.isEmpty()) {
            return List.of();
        }

        Map<Long, UserRef> technicianById = loadActiveTechnicians(slots);

        List<AvailableTechnician> result = new ArrayList<>();

        for (TechnicianAvailability slot : slots) {
            UserRef technician = technicianById.get(slot.technicianId());

            if (technician != null) {
                result.add(toAvailableTechnician(slot, technician));
            }
        }

        return result;
    }

    private Map<Long, UserRef> loadActiveTechnicians(List<TechnicianAvailability> slots) {

        List<Long> technicianIds = slots.stream()
                .map(TechnicianAvailability::technicianId)
                .distinct()
                .toList();

        return userDirectory.findActiveByIds(technicianIds).stream()
                .collect(Collectors.toMap(UserRef::id, ref -> ref));
    }

    private AvailableTechnician toAvailableTechnician(TechnicianAvailability slot, UserRef technician) {
        return new AvailableTechnician(
                slot.technicianId(),
                technician.name(),
                slot.id(),
                slot.startTime(),
                slot.endTime());
    }
}