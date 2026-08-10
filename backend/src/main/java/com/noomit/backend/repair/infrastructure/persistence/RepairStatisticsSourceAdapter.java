package com.noomit.backend.repair.infrastructure.persistence;

import com.noomit.backend.repair.RepairStatisticsSource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@Component
@RequiredArgsConstructor
class RepairStatisticsSourceAdapter implements RepairStatisticsSource {

    private final SpringDataRepairCaseRepository jpa;

    @Override
    @Transactional(readOnly = true)
    public List<RepairRecord> findByServiceRequestIds(Collection<Long> serviceRequestIds) {
        if (serviceRequestIds.isEmpty()) return List.of();
        return jpa.findByServiceRequestIdIn(serviceRequestIds).stream()
                .map(e -> new RepairRecord(
                        e.getServiceRequestId(),
                        RepairState.valueOf(e.getStatus().name()),
                        e.getTotalAmount()))
                .toList();
    }
}
