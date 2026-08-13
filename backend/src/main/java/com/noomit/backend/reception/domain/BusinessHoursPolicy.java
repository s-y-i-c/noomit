package com.noomit.backend.reception.domain;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/** 영업 정책: 월~금 09:00-18:00(12:00-13:00 제외), 1시간 단위. 토/일 휴무. */
public final class BusinessHoursPolicy {
    private static final LocalTime OPEN = LocalTime.of(9, 0);
    private static final LocalTime LUNCH_START = LocalTime.of(12, 0);
    private static final LocalTime CLOSE = LocalTime.of(18, 0);

    private BusinessHoursPolicy() {
    }

    public static boolean isBusinessDay(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
    }

    public static List<TimeRange> getBusinessHourSlots() {
        List<TimeRange> slots = new ArrayList<>();
        for (LocalTime start = OPEN; start.isBefore(CLOSE); start = start.plusHours(1)) {
            if (!start.equals(LUNCH_START)) {
                slots.add(new TimeRange(start, start.plusHours(1)));
            }
        }
        return slots;
    }

    public record TimeRange(LocalTime start, LocalTime end) {
    }
}