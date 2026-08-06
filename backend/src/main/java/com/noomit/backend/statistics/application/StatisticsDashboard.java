package com.noomit.backend.statistics.application;

import java.time.LocalDate;
import java.util.List;

public record StatisticsDashboard(
        Period period,
        Summary summary,
        RepeatRepair repeatRepair,
        List<TrendPoint> trends,
        List<TechnicianRow> technicians,
        List<CustomerRow> customers,
        List<ProductRow> products,
        Integration integration) {

    public record Period(LocalDate from, LocalDate to) {}

    public record Summary(long receivedCount, long completedCount, long inProgressCount,
            double completionRate, long averageProcessingMinutes, long medianProcessingMinutes) {}

    public record RepeatRepair(int windowDays, double sameCustomerRate,
            double sameProductRate, double sameTechnicianSameProductRate) {}

    public record TrendPoint(LocalDate date, long receivedCount, long completedCount) {}

    public record TechnicianRow(String technicianId, String technicianName, long assignedCount,
            long completedCount, double completionRate, long averageProcessingMinutes) {}

    public record CustomerRow(String customerId, String customerName, long requestCount,
            long completedCount, double repeatRate) {}

    public record ProductRow(String productId, String productName, long requestCount,
            long completedCount, double repeatRate) {}

    public record Integration(boolean connected, String message) {
        public static Integration ready() {
            return new Integration(true, "연결된 도메인의 최신 데이터를 조회했습니다.");
        }

        public static Integration waiting() {
            return new Integration(false, "접수·수리·고객·제품 도메인 연동을 기다리고 있습니다.");
        }
    }
}
