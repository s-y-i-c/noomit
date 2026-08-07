package com.noomit.backend.repair.infrastructure.persistence;

import com.noomit.backend.repair.application.port.RepairDetailRepository;
import com.noomit.backend.repair.domain.RepairDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
class JpaRepairDetailRepository implements RepairDetailRepository {

    private final SpringDataRepairDetailRepository jpa;

    @Override
    @Transactional
    public RepairDetail save(Long repairCaseId, String description, BigDecimal amount) {
        return toDomain(jpa.save(new RepairDetailEntity(repairCaseId, description, amount)));
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        jpa.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RepairDetail> findById(Long id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RepairDetail> findByRepairCaseId(Long repairCaseId) {
        return jpa.findByRepairCaseId(repairCaseId).stream().map(this::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal sumAmountByRepairCaseId(Long repairCaseId) {
        return jpa.sumAmountByRepairCaseId(repairCaseId);
    }

    private RepairDetail toDomain(RepairDetailEntity e) {
        return new RepairDetail(e.getId(), e.getRepairCaseId(),
                e.getDescription(), e.getAmount(), e.getCreatedAt());
    }
}
