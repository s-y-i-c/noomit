package com.noomit.backend.statistics.application;

import static java.util.function.Function.identity;

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

    public StatisticsDashboard getDashboard(StatisticsQuery query) {
        List<ReceptionSnapshot> receptions = List.copyOf(receptionReader.read(query));
        Set<String> requestIds = ids(receptions, ReceptionSnapshot::requestId);
        Map<String, RepairSnapshot> repairs = repairReader.readRepairs(requestIds).stream()
                .collect(Collectors.toMap(RepairSnapshot::requestId, identity(), (left, right) -> left));
        Map<String, String> customers = customerReader.readCustomers(ids(receptions, ReceptionSnapshot::customerId)).stream()
                .collect(Collectors.toMap(CustomerStatisticsReader.CustomerSnapshot::customerId,
                        CustomerStatisticsReader.CustomerSnapshot::customerName, (left, right) -> left));
        Map<String, String> products = productReader.readProducts(ids(receptions, ReceptionSnapshot::productId)).stream()
                .collect(Collectors.toMap(ProductStatisticsReader.ProductSnapshot::productId,
                        ProductStatisticsReader.ProductSnapshot::productName, (left, right) -> left));

        List<Long> processingMinutes = receptions.stream()
                .map(reception -> processingMinutes(reception, repairs.get(reception.requestId())))
                .filter(Objects::nonNull)
                .sorted()
                .toList();
        long completed = processingMinutes.size();
        long received = receptions.size();
        StatisticsDashboard.Summary summary = new StatisticsDashboard.Summary(
                received,
                completed,
                Math.max(0, received - completed),
                rate(completed, received),
                average(processingMinutes),
                median(processingMinutes));

        StatisticsDashboard.RepeatRepair repeatRepair = new StatisticsDashboard.RepeatRepair(
                query.repeatWindowDays(),
                repeatRate(receptions, ReceptionSnapshot::customerId, query.repeatWindowDays()),
                repeatRate(receptions, ReceptionSnapshot::customerProductId, query.repeatWindowDays()),
                repeatRate(receptions, item -> join(item.technicianId(), item.productId()), query.repeatWindowDays()));

        boolean connected = receptionReader.connected() && repairReader.connected()
                && customerReader.connected() && productReader.connected();
        return new StatisticsDashboard(
                new StatisticsDashboard.Period(query.from(), query.to()),
                summary,
                repeatRepair,
                trends(query, receptions, repairs),
                technicianRows(receptions, repairs),
                customerRows(receptions, repairs, customers, query.repeatWindowDays()),
                productRows(receptions, repairs, products, query.repeatWindowDays()),
                connected ? StatisticsDashboard.Integration.ready() : StatisticsDashboard.Integration.waiting());
    }

    private List<StatisticsDashboard.TrendPoint> trends(StatisticsQuery query,
            List<ReceptionSnapshot> receptions, Map<String, RepairSnapshot> repairs) {
        Map<LocalDate, Long> received = receptions.stream().collect(Collectors.groupingBy(
                item -> item.receivedAt().toLocalDate(), Collectors.counting()));
        Map<LocalDate, Long> completed = repairs.values().stream()
                .filter(item -> item.completedAt() != null)
                .filter(item -> !item.completedAt().toLocalDate().isBefore(query.from())
                        && !item.completedAt().toLocalDate().isAfter(query.to()))
                .collect(Collectors.groupingBy(item -> item.completedAt().toLocalDate(), Collectors.counting()));
        return query.from().datesUntil(query.to().plusDays(1))
                .map(date -> new StatisticsDashboard.TrendPoint(
                        date, received.getOrDefault(date, 0L), completed.getOrDefault(date, 0L)))
                .toList();
    }

    private List<TechnicianRow> technicianRows(List<ReceptionSnapshot> receptions,
            Map<String, RepairSnapshot> repairs) {
        Map<String, List<ReceptionSnapshot>> groups = group(receptions, ReceptionSnapshot::technicianId);
        return groups.entrySet().stream().map(entry -> {
            List<ReceptionSnapshot> items = entry.getValue();
            List<Long> minutes = processingMinutes(items, repairs);
            String name = items.stream().map(ReceptionSnapshot::technicianName)
                    .filter(value -> value != null && !value.isBlank()).findFirst().orElse(entry.getKey());
            return new TechnicianRow(entry.getKey(), name, items.size(), minutes.size(),
                    rate(minutes.size(), items.size()), average(minutes));
        }).sorted(Comparator.comparingLong(TechnicianRow::assignedCount).reversed()).toList();
    }

    private List<CustomerRow> customerRows(List<ReceptionSnapshot> receptions,
            Map<String, RepairSnapshot> repairs, Map<String, String> names, int windowDays) {
        Map<String, List<ReceptionSnapshot>> groups = group(receptions, ReceptionSnapshot::customerId);
        return groups.entrySet().stream().map(entry -> new CustomerRow(
                entry.getKey(), names.getOrDefault(entry.getKey(), entry.getKey()), entry.getValue().size(),
                processingMinutes(entry.getValue(), repairs).size(),
                repeatRate(entry.getValue(), ReceptionSnapshot::customerId, windowDays)))
                .sorted(Comparator.comparingLong(CustomerRow::requestCount).reversed()).toList();
    }

    private List<ProductRow> productRows(List<ReceptionSnapshot> receptions,
            Map<String, RepairSnapshot> repairs, Map<String, String> names, int windowDays) {
        Map<String, List<ReceptionSnapshot>> groups = group(receptions, ReceptionSnapshot::productId);
        return groups.entrySet().stream().map(entry -> new ProductRow(
                entry.getKey(), names.getOrDefault(entry.getKey(), entry.getKey()), entry.getValue().size(),
                processingMinutes(entry.getValue(), repairs).size(),
                repeatRate(entry.getValue(), ReceptionSnapshot::customerProductId, windowDays)))
                .sorted(Comparator.comparingLong(ProductRow::requestCount).reversed()).toList();
    }

    private Map<String, List<ReceptionSnapshot>> group(List<ReceptionSnapshot> receptions,
            Function<ReceptionSnapshot, String> classifier) {
        Map<String, List<ReceptionSnapshot>> groups = new LinkedHashMap<>();
        for (ReceptionSnapshot reception : receptions) {
            String key = classifier.apply(reception);
            if (key != null && !key.isBlank()) groups.computeIfAbsent(key, ignored -> new ArrayList<>()).add(reception);
        }
        return groups;
    }

    private List<Long> processingMinutes(Collection<ReceptionSnapshot> receptions,
            Map<String, RepairSnapshot> repairs) {
        return receptions.stream().map(item -> processingMinutes(item, repairs.get(item.requestId())))
                .filter(Objects::nonNull).sorted().toList();
    }

    private Long processingMinutes(ReceptionSnapshot reception, RepairSnapshot repair) {
        if (repair == null || repair.completedAt() == null || reception.receivedAt() == null) return null;
        long minutes = Duration.between(reception.receivedAt(), repair.completedAt()).toMinutes();
        return Math.max(0, minutes);
    }

    private double repeatRate(List<ReceptionSnapshot> receptions,
            Function<ReceptionSnapshot, String> keySelector, int windowDays) {
        Map<String, List<ReceptionSnapshot>> groups = new HashMap<>();
        for (ReceptionSnapshot reception : receptions) {
            String key = keySelector.apply(reception);
            if (key != null && !key.isBlank() && reception.receivedAt() != null) {
                groups.computeIfAbsent(key, ignored -> new ArrayList<>()).add(reception);
            }
        }
        long eligible = groups.values().stream().mapToLong(List::size).sum();
        long repeated = 0;
        for (List<ReceptionSnapshot> group : groups.values()) {
            group.sort(Comparator.comparing(ReceptionSnapshot::receivedAt));
            for (int index = 1; index < group.size(); index++) {
                long days = Duration.between(group.get(index - 1).receivedAt(), group.get(index).receivedAt()).toDays();
                if (days <= windowDays) repeated++;
            }
        }
        return rate(repeated, eligible);
    }

    private <T> Set<String> ids(Collection<T> values, Function<T, String> mapper) {
        return values.stream().map(mapper).filter(Objects::nonNull).filter(value -> !value.isBlank())
                .collect(Collectors.toUnmodifiableSet());
    }

    private String join(String left, String right) {
        return left == null || left.isBlank() || right == null || right.isBlank() ? null : left + "::" + right;
    }

    private long average(List<Long> values) {
        return values.isEmpty() ? 0 : Math.round(values.stream().mapToLong(Long::longValue).average().orElse(0));
    }

    private long median(List<Long> sorted) {
        if (sorted.isEmpty()) return 0;
        int middle = sorted.size() / 2;
        return sorted.size() % 2 == 1 ? sorted.get(middle) : Math.round((sorted.get(middle - 1) + sorted.get(middle)) / 2.0);
    }

    private double rate(long numerator, long denominator) {
        return denominator == 0 ? 0 : Math.round(numerator * 1000.0 / denominator) / 10.0;
    }
}
