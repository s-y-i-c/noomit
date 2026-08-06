package com.noomit.backend.statistics.infrastructure.demo;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import com.noomit.backend.statistics.application.StatisticsQuery;
import com.noomit.backend.statistics.application.StatisticsQuery.RequestStatus;
import com.noomit.backend.statistics.application.port.CustomerStatisticsReader;
import com.noomit.backend.statistics.application.port.ProductStatisticsReader;
import com.noomit.backend.statistics.application.port.ReceptionStatisticsReader;
import com.noomit.backend.statistics.application.port.RepairStatisticsReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 다른 도메인이 준비되기 전 통계 화면과 집계 기능을 확인하기 위한 데모 데이터 어댑터.
 * 프로파일 제한 없이 등록되므로 실제 조회 어댑터 도입 시 이 컴포넌트를 제거해야 한다.
 */
@Component
@RequiredArgsConstructor
class DummyStatisticsSourceAdapter implements ReceptionStatisticsReader, RepairStatisticsReader,
        CustomerStatisticsReader, ProductStatisticsReader {

    private final Clock clock;

    @Override
    public List<ReceptionSnapshot> read(StatisticsQuery query) {
        return receptions().stream()
                .filter(item -> !item.receivedAt().toLocalDate().isBefore(query.from()))
                .filter(item -> !item.receivedAt().toLocalDate().isAfter(query.to()))
                .filter(item -> query.technicianId() == null || query.technicianId().equals(item.technicianId()))
                .filter(item -> query.customerId() == null || query.customerId().equals(item.customerId()))
                .filter(item -> query.productId() == null || query.productId().equals(item.productId()))
                .filter(item -> query.status() == null || query.status() == item.status())
                .toList();
    }

    @Override
    public List<RepairSnapshot> readRepairs(Collection<String> requestIds) {
        Set<String> selected = Set.copyOf(requestIds);
        return repairs().stream().filter(item -> selected.contains(item.requestId())).toList();
    }

    @Override
    public List<CustomerSnapshot> readCustomers(Collection<String> customerIds) {
        Set<String> selected = Set.copyOf(customerIds);
        return customers().stream().filter(item -> selected.contains(item.customerId())).toList();
    }

    @Override
    public List<ProductSnapshot> readProducts(Collection<String> productIds) {
        Set<String> selected = Set.copyOf(productIds);
        return products().stream().filter(item -> selected.contains(item.productId())).toList();
    }

    @Override
    public boolean connected() {
        return true;
    }

    private List<ReceptionSnapshot> receptions() {
        return List.of(
                reception(1, 2, "customer-01", "customer-product-01", "product-01",
                        "technician-01", "김민준", RequestStatus.COMPLETED),
                reception(2, 5, "customer-02", "customer-product-02", "product-02",
                        "technician-02", "이서연", RequestStatus.COMPLETED),
                reception(3, 8, "customer-01", "customer-product-01", "product-01",
                        "technician-01", "김민준", RequestStatus.COMPLETED),
                reception(4, 11, "customer-03", "customer-product-03", "product-03",
                        "technician-03", "박도윤", RequestStatus.IN_PROGRESS),
                reception(5, 14, "customer-04", "customer-product-04", "product-04",
                        "technician-01", "김민준", RequestStatus.COMPLETED),
                reception(6, 17, "customer-05", "customer-product-05", "product-05",
                        "technician-02", "이서연", RequestStatus.ASSIGNED),
                reception(7, 20, "customer-06", "customer-product-06", "product-06",
                        "technician-03", "박도윤", RequestStatus.COMPLETED),
                reception(8, 23, "customer-07", "customer-product-07", "product-07",
                        "technician-01", "김민준", RequestStatus.CANCELLED),
                reception(9, 26, "customer-08", "customer-product-08", "product-08",
                        "technician-02", "이서연", RequestStatus.COMPLETED),
                reception(10, 29, "customer-09", "customer-product-09", "product-09",
                        "technician-03", "박도윤", RequestStatus.RECEIVED));
    }

    private List<RepairSnapshot> repairs() {
        return List.of(
                repair(1, 2, 6, 55_000),
                repair(2, 5, 18, 82_000),
                repair(3, 8, 3, 35_000),
                repair(4, 11, null, 0),
                repair(5, 14, 28, 120_000),
                repair(6, 17, null, 0),
                repair(7, 20, 9, 67_000),
                repair(8, 23, null, 0),
                repair(9, 26, 14, 74_000),
                repair(10, 29, null, 0));
    }

    private List<CustomerSnapshot> customers() {
        return List.of(
                new CustomerSnapshot("customer-01", "김하늘"),
                new CustomerSnapshot("customer-02", "이도현"),
                new CustomerSnapshot("customer-03", "박서윤"),
                new CustomerSnapshot("customer-04", "최지우"),
                new CustomerSnapshot("customer-05", "정우진"),
                new CustomerSnapshot("customer-06", "한예린"),
                new CustomerSnapshot("customer-07", "오민재"),
                new CustomerSnapshot("customer-08", "윤서아"),
                new CustomerSnapshot("customer-09", "장현우"),
                new CustomerSnapshot("customer-10", "임수빈"));
    }

    private List<ProductSnapshot> products() {
        return List.of(
                new ProductSnapshot("product-01", "비스포크 냉장고"),
                new ProductSnapshot("product-02", "스탠드 에어컨"),
                new ProductSnapshot("product-03", "드럼 세탁기"),
                new ProductSnapshot("product-04", "OLED TV"),
                new ProductSnapshot("product-05", "로봇 청소기"),
                new ProductSnapshot("product-06", "의류 관리기"),
                new ProductSnapshot("product-07", "식기세척기"),
                new ProductSnapshot("product-08", "공기청정기"),
                new ProductSnapshot("product-09", "전자레인지"),
                new ProductSnapshot("product-10", "김치냉장고"));
    }

    private ReceptionSnapshot reception(int index, int daysAgo, String customerId,
            String customerProductId, String productId, String technicianId,
            String technicianName, RequestStatus status) {
        return new ReceptionSnapshot(requestId(index), customerId, customerProductId, productId,
                technicianId, technicianName, daysAgo(daysAgo), status);
    }

    private RepairSnapshot repair(int index, int daysAgo, Integer processingHours, long cost) {
        LocalDateTime completedAt = processingHours == null ? null : daysAgo(daysAgo).plusHours(processingHours);
        return new RepairSnapshot(requestId(index), completedAt, cost);
    }

    private LocalDateTime daysAgo(int days) {
        return LocalDateTime.now(clock).minusDays(days).withHour(9).withMinute(0).withSecond(0).withNano(0);
    }

    private String requestId(int index) {
        return "request-%02d".formatted(index);
    }
}
