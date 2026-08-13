package com.noomit.backend.reception.infrastructure.scheduler;

import java.util.List;
import com.noomit.backend.reception.application.TechnicianAvailabilitySlotService;
import com.noomit.backend.user.UserDirectory;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** 활성 기사의 가능 시간 슬롯을 오늘 포함 향후 N일치까지 매일 채워 유지한다(윈도우 정책은 서비스가 소유). */
@Component
@RequiredArgsConstructor
class TechnicianAvailabilityScheduler {
    private final UserDirectory userDirectory;
    private final TechnicianAvailabilitySlotService slotService;

    @Scheduled(cron = "0 5 3 * * *", zone = "Asia/Seoul")
    public void generateUpcomingSlots() {
        List<Long> technicianIds = userDirectory.findActiveTechnicianIds();
        for (Long technicianId : technicianIds) {
            slotService.generateUpcomingSlotsForTechnician(technicianId);
        }
    }
}