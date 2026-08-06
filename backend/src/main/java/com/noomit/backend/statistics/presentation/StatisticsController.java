package com.noomit.backend.statistics.presentation;

import java.time.Clock;
import java.time.LocalDate;
import com.noomit.backend.shared.ApiResponse;
import com.noomit.backend.statistics.application.StatisticsDashboard;
import com.noomit.backend.statistics.application.StatisticsDashboardService;
import com.noomit.backend.statistics.application.StatisticsQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/statistics")
@RequiredArgsConstructor
class StatisticsController {
    private final StatisticsDashboardService dashboardService;
    private final Clock clock;

    @GetMapping("/dashboard")
    ApiResponse<StatisticsDashboard> dashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String technicianId,
            @RequestParam(required = false) String customerId,
            @RequestParam(required = false) String productId,
            @RequestParam(required = false) StatisticsQuery.RequestStatus status,
            @RequestParam(defaultValue = "30") int repeatWindowDays) {
        LocalDate today = LocalDate.now(clock);
        LocalDate resolvedTo = to == null ? today : to;
        LocalDate resolvedFrom = from == null ? resolvedTo.minusDays(29) : from;
        StatisticsQuery query = new StatisticsQuery(
                resolvedFrom,
                resolvedTo,
                technicianId,
                customerId,
                productId,
                status,
                repeatWindowDays);
        return ApiResponse.success(dashboardService.getDashboard(query));
    }
}
