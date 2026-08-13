package com.noomit.backend.reception.application;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import com.noomit.backend.reception.domain.BusinessHoursPolicy;
import org.springframework.stereotype.Component;

@Component
public class TechnicianAvailabilityGenerator {
    /** [from, toExclusive) 범위의 영업일 * 영업시간 슬롯을 생성 */
    public List<AvailabilitySlot> generate(long technicianId, LocalDate from, LocalDate toExclusive) {
        List<AvailabilitySlot> slots = new ArrayList<>();
        for (LocalDate date = from; date.isBefore(toExclusive); date = date.plusDays(1)) {
            if (!BusinessHoursPolicy.isBusinessDay(date)) {
                continue;
            }
            for (BusinessHoursPolicy.TimeRange range : BusinessHoursPolicy.getBusinessHourSlots()) {
                slots.add(new AvailabilitySlot(technicianId, date, range.start(), range.end()));
            }
        }
        return slots;
    }
}