package com.noomit.backend.statistics.infrastructure.repair;

import java.util.Collection;
import java.util.List;
import com.noomit.backend.repair.RepairStatisticsSource;
import com.noomit.backend.statistics.application.port.RepairStatisticsReader;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/** 수리 공개 계약을 통계 출력 포트로 변환하는 선택적 연결 어댑터입니다. */
@Component
@RequiredArgsConstructor
class RepairStatisticsAdapter implements RepairStatisticsReader {
    private final ObjectProvider<RepairStatisticsSource> sourceProvider;

    @Override
    public List<RepairSnapshot> readRepairs(Collection<Long> serviceRequestIds) {
        RepairStatisticsSource source = sourceProvider.getIfUnique();
        if (source == null || serviceRequestIds.isEmpty()) return List.of();

        return source.findByServiceRequestIds(serviceRequestIds).stream()
                .map(item -> new RepairSnapshot(
                        item.serviceRequestId(),
                        RepairState.valueOf(item.status().name()),
                        item.totalAmount()))
                .toList();
    }

    @Override
    public boolean connected() {
        return sourceProvider.getIfUnique() != null;
    }
}
