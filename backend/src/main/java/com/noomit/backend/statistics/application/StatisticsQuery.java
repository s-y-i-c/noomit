package com.noomit.backend.statistics.application;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public record StatisticsQuery(
        LocalDate from,
        LocalDate to,
        String technicianId,
        String customerId,
        String productId,
        RequestStatus status,
        int repeatWindowDays) {

    private static final int MAX_PERIOD_DAYS = 366;

    public StatisticsQuery {
        if (from == null || to == null) {
            throw new IllegalArgumentException("조회 시작일과 종료일은 필수입니다.");
        }
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("조회 시작일은 종료일보다 늦을 수 없습니다.");
        }
        if (ChronoUnit.DAYS.between(from, to) + 1 > MAX_PERIOD_DAYS) {
            throw new IllegalArgumentException("조회 기간은 최대 1년입니다.");
        }
        if (repeatWindowDays < 1 || repeatWindowDays > 365) {
            throw new IllegalArgumentException("재수리 판정 기간은 1일부터 365일 사이여야 합니다.");
        }
        technicianId = normalize(technicianId);
        customerId = normalize(customerId);
        productId = normalize(productId);
    }

    public static StatisticsQuery defaults(LocalDate today) {
        return new StatisticsQuery(today.minusDays(29), today, null, null, null, null, 30);
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public enum RequestStatus {
        RECEIVED,
        ASSIGNED,
        IN_PROGRESS,
        COMPLETED,
        CANCELLED
    }
}
