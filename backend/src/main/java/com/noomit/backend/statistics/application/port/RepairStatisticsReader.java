package com.noomit.backend.statistics.application.port;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

/** 통계 도메인이 수리 데이터에 기대하는 최소 일괄 읽기 계약. */
public interface RepairStatisticsReader {
    List<RepairSnapshot> readRepairs(Collection<Long> serviceRequestIds);

    default boolean connected() {
        return true;
    }

    record RepairSnapshot(
            long serviceRequestId,
            RepairState status,
            BigDecimal totalAmount) {}

    enum RepairState {
        IN_PROGRESS,
        SUBMITTED,
        COMPLETED
    }
}
