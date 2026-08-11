package com.noomit.backend.statistics.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collection;
import java.util.List;
import com.noomit.backend.statistics.application.port.CustomerStatisticsReader;
import com.noomit.backend.statistics.application.port.ProductStatisticsReader;
import com.noomit.backend.statistics.application.port.ReceptionStatisticsReader;
import com.noomit.backend.statistics.application.port.ReceptionStatisticsReader.ReceptionState;
import com.noomit.backend.statistics.application.port.RepairStatisticsReader;
import com.noomit.backend.statistics.application.port.RepairStatisticsReader.RepairState;
import org.junit.jupiter.api.Test;

class StatisticsDashboardServiceTest {

    @Test
    void aggregatesSnapshotsWithoutOwningAStatisticsDatabase() {
        Instant first = Instant.parse("2026-08-01T00:00:00Z");
        Instant second = Instant.parse("2026-08-10T00:00:00Z");
        ReceptionStatisticsReader receptions = _ -> List.of(
                new ReceptionStatisticsReader.ReceptionSnapshot(
                        1L, 10L, 20L, 30L, "김기사", first, ReceptionState.ASSIGNED),
                new ReceptionStatisticsReader.ReceptionSnapshot(
                        2L, 10L, 20L, 30L, "김기사", second, ReceptionState.ASSIGNED));
        RepairStatisticsReader repairs = _ -> List.of(
                new RepairStatisticsReader.RepairSnapshot(
                        1L, RepairState.COMPLETED, new BigDecimal("30000")),
                new RepairStatisticsReader.RepairSnapshot(
                        2L, RepairState.IN_PROGRESS, new BigDecimal("10000")));
        CustomerStatisticsReader customers = _ -> List.of(
                new CustomerStatisticsReader.CustomerSnapshot(10L, "홍길동"));
        ProductStatisticsReader products = _ -> List.of(
                new ProductStatisticsReader.ProductSnapshot(20L, "냉장고 A"));

        StatisticsDashboardService service = new StatisticsDashboardService(
                receptions,
                repairs,
                customers,
                products,
                Clock.fixed(Instant.parse("2026-08-10T00:00:00Z"), ZoneId.of("Asia/Seoul")));
        StatisticsDashboard result = service.getDashboard(new StatisticsQuery(
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 31),
                null, null, null, null, 30));

        assertThat(result.summary().receivedCount()).isEqualTo(2);
        assertThat(result.summary().completedCount()).isEqualTo(1);
        assertThat(result.summary().inProgressCount()).isEqualTo(1);
        assertThat(result.summary().completionRate()).isEqualTo(50.0);
        assertThat(result.summary().totalRepairAmount()).isEqualByComparingTo("30000");
        assertThat(result.repeatRepair().sameCustomerRate()).isEqualTo(50.0);
        assertThat(result.repeatRepair().sameProductRate()).isEqualTo(50.0);
        assertThat(result.technicians()).singleElement().satisfies(row -> {
            assertThat(row.technicianName()).isEqualTo("김기사");
            assertThat(row.assignedCount()).isEqualTo(2);
            assertThat(row.totalRepairAmount()).isEqualByComparingTo("30000");
        });
        assertThat(result.customers()).singleElement().satisfies(row ->
                assertThat(row.customerName()).isEqualTo("홍길동"));
        assertThat(result.products()).singleElement().satisfies(row ->
                assertThat(row.productName()).isEqualTo("냉장고 A"));
        assertThat(result.integration().receptionConnected()).isTrue();
        assertThat(result.integration().repairConnected()).isTrue();
        assertThat(result.integration().customerConnected()).isTrue();
        assertThat(result.integration().productConnected()).isTrue();
    }

    @Test
    void keepsReceptionStatisticsAvailableWhileOptionalDomainsAreDisconnected() {
        ReceptionStatisticsReader receptions = _ -> List.of(
                new ReceptionStatisticsReader.ReceptionSnapshot(
                        1L, 10L, 20L, 30L, null,
                        Instant.parse("2026-08-01T00:00:00Z"), ReceptionState.ASSIGNED));
        RepairStatisticsReader repairs = new RepairStatisticsReader() {
            @Override
            public List<RepairSnapshot> readRepairs(Collection<Long> serviceRequestIds) {
                return List.of();
            }

            @Override
            public boolean connected() {
                return false;
            }
        };
        CustomerStatisticsReader customers = new CustomerStatisticsReader() {
            @Override
            public List<CustomerSnapshot> readCustomers(Collection<Long> customerIds) {
                return List.of();
            }

            @Override
            public boolean connected() {
                return false;
            }
        };
        ProductStatisticsReader products = new ProductStatisticsReader() {
            @Override
            public List<ProductSnapshot> readProducts(Collection<Long> productIds) {
                return List.of();
            }

            @Override
            public boolean connected() {
                return false;
            }
        };

        StatisticsDashboardService service = new StatisticsDashboardService(
                receptions, repairs, customers, products,
                Clock.fixed(Instant.parse("2026-08-10T00:00:00Z"), ZoneId.of("Asia/Seoul")));
        StatisticsDashboard result = service.getDashboard(new StatisticsQuery(
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31),
                null, null, null, null, 30));

        assertThat(result.summary().receivedCount()).isEqualTo(1);
        assertThat(result.integration().receptionConnected()).isTrue();
        assertThat(result.integration().repairConnected()).isFalse();
        assertThat(result.integration().customerConnected()).isFalse();
        assertThat(result.integration().productConnected()).isFalse();
        assertThat(result.customers()).singleElement().satisfies(row ->
                assertThat(row.customerName()).isEqualTo("10"));
        assertThat(result.products()).singleElement().satisfies(row ->
                assertThat(row.productName()).isEqualTo("20"));
    }

    @Test
    void includesReceptionsWithoutAProductInBaseStatisticsButNotProductStatistics() {
        ReceptionStatisticsReader receptions = _ -> List.of(
                new ReceptionStatisticsReader.ReceptionSnapshot(
                        1L, 10L, null, 30L, "김기사",
                        Instant.parse("2026-08-01T00:00:00Z"), ReceptionState.ASSIGNED),
                new ReceptionStatisticsReader.ReceptionSnapshot(
                        2L, 10L, 20L, 30L, "김기사",
                        Instant.parse("2026-08-10T00:00:00Z"), ReceptionState.ASSIGNED));
        RepairStatisticsReader repairs = _ -> List.of();
        CustomerStatisticsReader customers = _ -> List.of(
                new CustomerStatisticsReader.CustomerSnapshot(10L, "홍길동"));
        ProductStatisticsReader products = productIds -> {
            assertThat(productIds).containsExactly(20L);
            return List.of(new ProductStatisticsReader.ProductSnapshot(20L, "냉장고 A"));
        };

        StatisticsDashboardService service = new StatisticsDashboardService(
                receptions, repairs, customers, products,
                Clock.fixed(Instant.parse("2026-08-10T00:00:00Z"), ZoneId.of("Asia/Seoul")));
        StatisticsDashboard result = service.getDashboard(new StatisticsQuery(
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31),
                null, null, null, null, 30));

        assertThat(result.summary().receivedCount()).isEqualTo(2);
        assertThat(result.customers()).singleElement().satisfies(row ->
                assertThat(row.requestCount()).isEqualTo(2));
        assertThat(result.technicians()).singleElement().satisfies(row ->
                assertThat(row.assignedCount()).isEqualTo(2));
        assertThat(result.products()).singleElement().satisfies(row -> {
            assertThat(row.productId()).isEqualTo("20");
            assertThat(row.requestCount()).isEqualTo(1);
        });
        assertThat(result.repeatRepair().sameProductRate()).isZero();
        assertThat(result.repeatRepair().sameTechnicianSameProductRate()).isZero();
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
