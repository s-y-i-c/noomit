package com.noomit.backend.statistics.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import com.noomit.backend.statistics.application.StatisticsQuery.RequestStatus;
import com.noomit.backend.statistics.application.port.CustomerStatisticsReader;
import com.noomit.backend.statistics.application.port.ProductStatisticsReader;
import com.noomit.backend.statistics.application.port.ReceptionStatisticsReader;
import com.noomit.backend.statistics.application.port.RepairStatisticsReader;
import org.junit.jupiter.api.Test;

class StatisticsDashboardServiceTest {

    @Test
    void aggregatesSnapshotsWithoutOwningAStatisticsDatabase() {
        LocalDateTime first = LocalDateTime.of(2026, 8, 1, 9, 0);
        LocalDateTime second = LocalDateTime.of(2026, 8, 10, 9, 0);
        ReceptionStatisticsReader receptions = query -> List.of(
                new ReceptionStatisticsReader.ReceptionSnapshot(
                        "r1", "c1", "cp1", "p1", "t1", "김기사", first, RequestStatus.COMPLETED),
                new ReceptionStatisticsReader.ReceptionSnapshot(
                        "r2", "c1", "cp1", "p1", "t1", "김기사", second, RequestStatus.IN_PROGRESS));
        RepairStatisticsReader repairs = requestIds -> List.of(
                new RepairStatisticsReader.RepairSnapshot("r1", first.plusHours(2), 30_000));
        CustomerStatisticsReader customers = customerIds -> List.of(
                new CustomerStatisticsReader.CustomerSnapshot("c1", "홍길동"));
        ProductStatisticsReader products = productIds -> List.of(
                new ProductStatisticsReader.ProductSnapshot("p1", "냉장고 A"));

        StatisticsDashboardService service = new StatisticsDashboardService(
                receptions, repairs, customers, products);
        StatisticsDashboard result = service.getDashboard(new StatisticsQuery(
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 31),
                null, null, null, null, 30));

        assertThat(result.summary().receivedCount()).isEqualTo(2);
        assertThat(result.summary().completedCount()).isEqualTo(1);
        assertThat(result.summary().completionRate()).isEqualTo(50.0);
        assertThat(result.summary().averageProcessingMinutes()).isEqualTo(120);
        assertThat(result.summary().medianProcessingMinutes()).isEqualTo(120);
        assertThat(result.repeatRepair().sameCustomerRate()).isEqualTo(50.0);
        assertThat(result.repeatRepair().sameProductRate()).isEqualTo(50.0);
        assertThat(result.technicians()).singleElement().satisfies(row -> {
            assertThat(row.technicianName()).isEqualTo("김기사");
            assertThat(row.assignedCount()).isEqualTo(2);
        });
        assertThat(result.customers()).singleElement().satisfies(row ->
                assertThat(row.customerName()).isEqualTo("홍길동"));
        assertThat(result.products()).singleElement().satisfies(row ->
                assertThat(row.productName()).isEqualTo("냉장고 A"));
        assertThat(result.integration().connected()).isTrue();
    }

    @Test
    void rejectsPeriodsLongerThanOneYear() {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () ->
                new StatisticsQuery(
                        LocalDate.of(2025, 1, 1),
                        LocalDate.of(2026, 1, 2),
                        null, null, null, null, 30));
    }
}
