package com.noomit.backend.statistics.application.port;

import java.time.LocalDateTime;
import java.util.List;
import com.noomit.backend.statistics.application.StatisticsQuery;

/** 통계 도메인이 접수 데이터에 기대하는 최소 읽기 계약. */
public interface ReceptionStatisticsReader {
    List<ReceptionSnapshot> read(StatisticsQuery query);

    default boolean connected() {
        return true;
    }

    record ReceptionSnapshot(
            String requestId,
            String customerId,
            String customerProductId,
            String productId,
            String technicianId,
            String technicianName,
            LocalDateTime receivedAt,
            StatisticsQuery.RequestStatus status) {}
}
