package com.noomit.backend.reception.application;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import com.noomit.backend.user.EngineerRoleGranted;
import com.noomit.backend.user.EngineerRoleRevoked;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TechnicianAvailabilitySlotService {
    /** 기사 가능 시간 슬롯 오늘 포함 향후 30일 유지 */
    private static final int ROLLING_WINDOW_DAYS = 30;

    private final TechnicianAvailabilityRepository availabilityRepository;
    private final TechnicianAvailabilityGenerator availabilityGenerator;
    private final Clock clock;

    /** 오늘 포함 {@value #ROLLING_WINDOW_DAYS}일치 슬롯을 생성 */
    @Transactional
    public void generateUpcomingSlotsForTechnician(long technicianId) {
        LocalDate today = LocalDate.now(clock);
        generateSlotsForTechnician(technicianId, today, today.plusDays(ROLLING_WINDOW_DAYS));
    }

    /** 오늘 이후, 배정된 접수가 없는 슬롯만 정리 */
    @Transactional
    public void releaseFutureSlotsForTechnician(long technicianId) {
        availabilityRepository.deleteUnassignedFutureSlots(technicianId, LocalDate.now(clock));
    }

    @ApplicationModuleListener
    public void onEngineerRoleGranted(EngineerRoleGranted event) {
        generateUpcomingSlotsForTechnician(event.userId());
    }

    @ApplicationModuleListener
    public void onEngineerRoleRevoked(EngineerRoleRevoked event) {
        releaseFutureSlotsForTechnician(event.userId());
    }

    private void generateSlotsForTechnician(long technicianId, LocalDate from, LocalDate toExclusive) {
        List<AvailabilitySlot> slots = availabilityGenerator.generate(technicianId, from, toExclusive);
        availabilityRepository.insertAllIfAbsent(slots);
    }
}