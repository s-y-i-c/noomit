package com.noomit.backend.reception.infrastructure.scheduler;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import com.noomit.backend.reception.application.TechnicianAvailabilityService;
import com.noomit.backend.user.UserDirectory;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** 활성 기사의 가능 시간 슬롯을 오늘 포함 향후 30일치까지 매일 채워 유지한다. */
@Component
@RequiredArgsConstructor
class TechnicianAvailabilityScheduler {
    private static final int ROLLING_WINDOW_DAYS = 30;

    private final UserDirectory userDirectory;
    private final TechnicianAvailabilityService availabilityService;
    private final Clock clock;

    @Scheduled(cron = "0 5 3 * * *", zone = "Asia/Seoul")
    public void generateUpcomingSlots() {
        LocalDate today = LocalDate.now(clock);
        LocalDate toExclusive = today.plusDays(ROLLING_WINDOW_DAYS);

        List<Long> technicianIds = userDirectory.findActiveTechnicianIds();
        for (Long technicianId : technicianIds) {
            availabilityService.generateSlotsForTechnician(technicianId, today, toExclusive);
        }
    }
}