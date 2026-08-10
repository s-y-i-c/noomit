package com.noomit.backend.statistics.application;

import static java.util.function.Function.identity;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import com.noomit.backend.statistics.application.StatisticsDashboard.CustomerRow;
import com.noomit.backend.statistics.application.StatisticsDashboard.ProductRow;
import com.noomit.backend.statistics.application.StatisticsDashboard.TechnicianRow;
import com.noomit.backend.statistics.application.StatisticsQuery.RequestStatus;
import com.noomit.backend.statistics.application.port.CustomerStatisticsReader;
import com.noomit.backend.statistics.application.port.ProductStatisticsReader;
import com.noomit.backend.statistics.application.port.ReceptionStatisticsReader;
import com.noomit.backend.statistics.application.port.ReceptionStatisticsReader.ReceptionSnapshot;
import com.noomit.backend.statistics.application.port.RepairStatisticsReader;
import com.noomit.backend.statistics.application.port.RepairStatisticsReader.RepairSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StatisticsDashboardService {
    private final ReceptionStatisticsReader receptionReader;
    private final RepairStatisticsReader repairReader;
    private final CustomerStatisticsReader customerReader;
    private final ProductStatisticsReader productReader;
    private final Clock clock;

    public StatisticsDashboard getDashboard(StatisticsQuery query) {
        List<ReceptionSnapshot> allReceptions = List.copyOf(receptionReader.read(query));
        Set<Long> serviceRequestIds = ids(allReceptions, ReceptionSnapshot::serviceRequestId);
        Map<Long, RepairSnapshot> repairs = repairReader.readRepairs(serviceRequestIds).stream()
                .collect(Collectors.toMap(RepairSnapshot::serviceRequestId, identity()));

        List<ReceptionSnapshot> receptions = allReceptions.stream()
                .filter(item -> query.status() == null
                        || query.status() == currentStatus(item, repairs.get(item.serviceRequestId())))
                .toList();
        Map<Long, String> customers = customerReader.readCustomers(ids(receptions, ReceptionSnapshot::customerId)).stream()
                .collect(Collectors.toMap(CustomerStatisticsReader.CustomerSnapshot::customerId,
                        CustomerStatisticsReader.CustomerSnapshot::customerName));
        Map<Long, String> products = productReader.readProducts(ids(receptions, ReceptionSnapshot::productId)).stream()
                .collect(Collectors.toMap(ProductStatisticsReader.ProductSnapshot::productId,
                        ProductStatisticsReader.ProductSnapshot::productName));

        long received = receptions.size();
        long completed = countByStatus(receptions, repairs, RequestStatus.COMPLETED);
        long inProgress = countByStatus(receptions, repairs, RequestStatus.IN_PROGRESS);
        long cancelled = countByStatus(receptions, repairs, RequestStatus.CANCELLED);
        StatisticsDashboard.Summary summary = new StatisticsDashboard.Summary(
                received,
                completed,
                inProgress,
                cancelled,
                rate(completed, received),
                totalRepairAmount(receptions, repairs));

        StatisticsDashboard.RepeatRepair repeatRepair = new StatisticsDashboard.RepeatRepair(
                query.repeatWindowDays(),
                repeatRate(receptions, ReceptionSnapshot::customerId, query.repeatWindowDays()),
                repeatRate(receptions,
                        item -> new RepeatKey(item.customerId(), item.productId()), query.repeatWindowDays()),
                repeatRate(receptions,
                        item -> item.technicianId() == null
                                ? null : new RepeatKey(item.technicianId(), item.productId()),
                        query.repeatWindowDays()));

        StatisticsDashboard.Integration integration = StatisticsDashboard.Integration.of(
                receptionReader.connected(),
                repairReader.connected(),
                customerReader.connected(),
                productReader.connected());
        return new StatisticsDashboard(
                new StatisticsDashboard.Period(query.from(), query.to()),
                summary,
                repeatRepair,
                trends(query, receptions),
                technicianRows(receptions, repairs),
                customerRows(receptions, repairs, customers, query.repeatWindowDays()),
                productRows(receptions, repairs, products, query.repeatWindowDays()),
                integration);
    }

    private List<StatisticsDashboard.TrendPoint> trends(
            StatisticsQuery query, List<ReceptionSnapshot> receptions) {
        Map<LocalDate, Long> received = receptions.stream().collect(Collectors.groupingBy(
                item -> item.requestedAt().atZone(clock.getZone()).toLocalDate(), Collectors.counting()));
        return query.from().datesUntil(query.to().plusDays(1))
                .map(date -> new StatisticsDashboard.TrendPoint(date, received.getOrDefault(date, 0L)))
                .toList();
    }

    private List<TechnicianRow> technicianRows(
            List<ReceptionSnapshot> receptions, Map<Long, RepairSnapshot> repairs) {
        Map<Long, List<ReceptionSnapshot>> groups = group(receptions, ReceptionSnapshot::technicianId);
        return groups.entrySet().stream().map(entry -> {
            List<ReceptionSnapshot> items = entry.getValue();
            long completed = countByStatus(items, repairs, RequestStatus.COMPLETED);
            String name = items.stream().map(ReceptionSnapshot::technicianName)
                    .filter(value -> value != null && !value.isBlank())
                    .findFirst().orElse(Long.toString(entry.getKey()));
            return new TechnicianRow(
                    Long.toString(entry.getKey()),
                    name,
                    items.size(),
                    completed,
                    rate(completed, items.size()),
                    totalRepairAmount(items, repairs));
        }).sorted(Comparator.comparingLong(TechnicianRow::assignedCount).reversed()).toList();
    }

    private List<CustomerRow> customerRows(
            List<ReceptionSnapshot> receptions,
            Map<Long, RepairSnapshot> repairs,
            Map<Long, String> names,
            int windowDays) {
        Map<Long, List<ReceptionSnapshot>> groups = group(receptions, ReceptionSnapshot::customerId);
        return groups.entrySet().stream().map(entry -> new CustomerRow(
                Long.toString(entry.getKey()),
                names.getOrDefault(entry.getKey(), Long.toString(entry.getKey())),
                entry.getValue().size(),
                countByStatus(entry.getValue(), repairs, RequestStatus.COMPLETED),
                repeatRate(entry.getValue(), ReceptionSnapshot::customerId, windowDays)))
                .sorted(Comparator.comparingLong(CustomerRow::requestCount).reversed()).toList();
    }

    private List<ProductRow> productRows(
            List<ReceptionSnapshot> receptions,
            Map<Long, RepairSnapshot> repairs,
            Map<Long, String> names,
            int windowDays) {
        Map<Long, List<ReceptionSnapshot>> groups = group(receptions, ReceptionSnapshot::productId);
        return groups.entrySet().stream().map(entry -> new ProductRow(
                Long.toString(entry.getKey()),
                names.getOrDefault(entry.getKey(), Long.toString(entry.getKey())),
                entry.getValue().size(),
                countByStatus(entry.getValue(), repairs, RequestStatus.COMPLETED),
                repeatRate(entry.getValue(),
                        item -> new RepeatKey(item.customerId(), item.productId()), windowDays)))
                .sorted(Comparator.comparingLong(ProductRow::requestCount).reversed()).toList();
    }

    private RequestStatus currentStatus(ReceptionSnapshot reception, RepairSnapshot repair) {
        if (reception.status() == ReceptionStatisticsReader.ReceptionState.CANCELLED) {
            return RequestStatus.CANCELLED;
        }
        if (repair != null) {
            return switch (repair.status()) {
                case COMPLETED -> RequestStatus.COMPLETED;
                case IN_PROGRESS, SUBMITTED -> RequestStatus.IN_PROGRESS;
            };
        }
        return reception.status() == ReceptionStatisticsReader.ReceptionState.RECEIVED
                ? RequestStatus.RECEIVED : RequestStatus.ASSIGNED;
    }

    private long countByStatus(
            Collection<ReceptionSnapshot> receptions,
            Map<Long, RepairSnapshot> repairs,
            RequestStatus status) {
        return receptions.stream()
                .filter(item -> currentStatus(item, repairs.get(item.serviceRequestId())) == status)
                .count();
    }

    private BigDecimal totalRepairAmount(
            Collection<ReceptionSnapshot> receptions, Map<Long, RepairSnapshot> repairs) {
        return receptions.stream()
                .map(item -> repairs.get(item.serviceRequestId()))
                .filter(Objects::nonNull)
                .map(RepairSnapshot::totalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private <K> Map<K, List<ReceptionSnapshot>> group(
            List<ReceptionSnapshot> receptions, Function<ReceptionSnapshot, K> classifier) {
        Map<K, List<ReceptionSnapshot>> groups = new LinkedHashMap<>();
        for (ReceptionSnapshot reception : receptions) {
            K key = classifier.apply(reception);
            if (key != null) groups.computeIfAbsent(key, ignored -> new ArrayList<>()).add(reception);
        }
        return groups;
    }

    private <K> double repeatRate(
            List<ReceptionSnapshot> receptions,
            Function<ReceptionSnapshot, K> keySelector,
            int windowDays) {
        Map<K, List<ReceptionSnapshot>> groups = new HashMap<>();
        for (ReceptionSnapshot reception : receptions) {
            K key = keySelector.apply(reception);
            if (key != null && reception.requestedAt() != null) {
                groups.computeIfAbsent(key, ignored -> new ArrayList<>()).add(reception);
            }
        }
        long eligible = groups.values().stream().mapToLong(List::size).sum();
        long repeated = 0;
        for (List<ReceptionSnapshot> group : groups.values()) {
            group.sort(Comparator.comparing(ReceptionSnapshot::requestedAt));
            for (int index = 1; index < group.size(); index++) {
                long days = Duration.between(
                        group.get(index - 1).requestedAt(), group.get(index).requestedAt()).toDays();
                if (days <= windowDays) repeated++;
            }
        }
        return rate(repeated, eligible);
    }

    private <T> Set<Long> ids(Collection<T> values, Function<T, Long> mapper) {
        return values.stream().map(mapper).filter(Objects::nonNull).collect(Collectors.toUnmodifiableSet());
    }

    private double rate(long numerator, long denominator) {
        return denominator == 0 ? 0 : Math.round(numerator * 1000.0 / denominator) / 10.0;
    }

    private record RepeatKey(long first, long second) {}
}
