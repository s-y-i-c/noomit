package com.noomit.backend.reception.infrastructure.persistence;

import com.noomit.backend.reception.ReceptionStatisticsSource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
class ReceptionStatisticsSourceJpaAdapter implements ReceptionStatisticsSource {

    private final ServiceRequestJpaRepository jpaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ReceptionRecord> find(ReceptionCriteria criteria) {
        return jpaRepository.findForStatistics(
                        criteria.fromInclusive(),
                        criteria.toExclusive(),
                        criteria.technicianId(),
                        criteria.customerId(),
                        criteria.productId())
                .stream()
                .map(e -> new ReceptionRecord(
                        e.getId(),
                        e.getCustomerId(),
                        e.getProductId(),
                        e.getTechnicianId(),
                        e.getRequestedAt(),
                        ReceptionState.valueOf(e.getStatus().name())))
                .toList();
    }
}