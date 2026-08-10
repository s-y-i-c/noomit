package com.noomit.backend.reception.application;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalTime;
import java.util.Map;
import java.util.stream.Collectors;
import com.noomit.backend.reception.domain.TechnicianAvailability;
import com.noomit.backend.reception.domain.TechnicianAvailabilityStatus;
import com.noomit.backend.shared.error.BusinessException;
import com.noomit.backend.shared.error.ErrorCode;
import com.noomit.backend.user.UserDirectory;
import com.noomit.backend.user.UserRef;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TechnicianAvailabilityService {
    private final TechnicianAvailabilityQueryRepository availabilityQueryRepository;
    private final TechnicianAvailabilityRepository availabilityRepository;
    private final UserDirectory userDirectory;
    private final Clock clock;

    public List<AvailabilityTimeSlot> getByDate(LocalDate date) {
        return availabilityQueryRepository.findByDate(date);
    }
    
    public List<LocalDate> getAvailableDates() {
        return availabilityQueryRepository.findAvailableDatesFrom(LocalDate.now(clock));
    }

    public List<AvailableTechnician> getAvailableTechnicians(LocalDate date, LocalTime startTime, LocalTime endTime) {
        List<TechnicianAvailability> slots = availabilityQueryRepository.findAvailableSlots(date, startTime, endTime);

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

    public List<MyAvailabilitySlot> getMyAvailabilitySlots(long technicianId, LocalDate date) {
        return availabilityQueryRepository.findByTechnicianAndDate(technicianId, date);
    }

    @Transactional
    public MyAvailabilitySlot updateAvailabilityStatus(long technicianId, long slotId, TechnicianAvailabilityStatus status) {
        TechnicianAvailability slot = availabilityRepository.findById(slotId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RECEPTION_NOT_FOUND, "슬롯을 찾을 수 없습니다."));

        if (!slot.isOwnedBy(technicianId)) {
            throw new BusinessException(ErrorCode.RECEPTION_SLOT_NOT_OWNED, "본인 슬롯만 변경할 수 있습니다.");
        }

        int updated = availabilityRepository.updateAvailabilityStatus(slotId, technicianId, status);
        if (updated == 0) {
            throw new BusinessException(ErrorCode.RECEPTION_SLOT_ALREADY_BOOKED, "배정된 접수가 있는 슬롯은 변경할 수 없습니다.");
        }

        return new MyAvailabilitySlot(slotId, slot.startTime(), slot.endTime(), status, false);
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