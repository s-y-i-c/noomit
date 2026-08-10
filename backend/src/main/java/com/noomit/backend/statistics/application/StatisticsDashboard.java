package com.noomit.backend.statistics.application;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
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
            long cancelledCount, double completionRate, BigDecimal totalRepairAmount) {}

    public record RepeatRepair(int windowDays, double sameCustomerRate,
            double sameProductRate, double sameTechnicianSameProductRate) {}

    public record TrendPoint(LocalDate date, long receivedCount) {}

    public record TechnicianRow(String technicianId, String technicianName, long assignedCount,
            long completedCount, double completionRate, BigDecimal totalRepairAmount) {}

    public record CustomerRow(String customerId, String customerName, long requestCount,
            long completedCount, double repeatRate) {}

    public record ProductRow(String productId, String productName, long requestCount,
            long completedCount, double repeatRate) {}

    public record Integration(
            boolean receptionConnected,
            boolean repairConnected,
            boolean customerConnected,
            boolean productConnected,
            String message) {

        public static Integration of(boolean reception, boolean repair, boolean customer, boolean product) {
            if (!reception) {
                return new Integration(false, repair, customer, product,
                        "기본 통계에 필요한 접수 도메인 연동을 기다리고 있습니다.");
            }

            List<String> waiting = new ArrayList<>();
            if (!repair) waiting.add("수리");
            if (!customer) waiting.add("고객");
            if (!product) waiting.add("제품");
            String message = waiting.isEmpty()
                    ? "연결된 도메인의 최신 데이터를 조회했습니다."
                    : String.join("·", waiting) + " 도메인은 연동 전이며, 연결된 데이터만 표시합니다.";
            return new Integration(true, repair, customer, product, message);
        }
    }
}
