package com.noomit.backend.repair.infrastructure.persistence;

import com.noomit.backend.repair.application.port.RepairCaseRepository;
import com.noomit.backend.repair.domain.RepairCase;
import com.noomit.backend.repair.domain.RepairDetail;
import com.noomit.backend.repair.domain.RepairStatus;
import com.noomit.backend.shared.error.BusinessException;
import com.noomit.backend.shared.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
class JpaRepairCaseRepository implements RepairCaseRepository {

    private final SpringDataRepairCaseRepository jpa;
    private final SpringDataRepairDetailRepository detailJpa;

    @Override
    @Transactional(readOnly = true)
    public Optional<RepairCase> findById(Long id) {
        return jpa.findById(id).map(e -> toDomain(e, detailJpa.findByRepairCaseId(e.getId())));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RepairCase> findByTechnicianId(Long technicianId) {
        List<RepairCaseEntity> cases = jpa.findByTechnicianId(technicianId);
        return toDomainsWithBatchDetails(cases);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RepairCase> findAll() {
        List<RepairCaseEntity> cases = jpa.findAll();
        return toDomainsWithBatchDetails(cases);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RepairCase> findAllByStatus(RepairStatus status) {
        List<RepairCaseEntity> cases = jpa.findByStatus(status);
        return toDomainsWithBatchDetails(cases);
    }

    @Override
    @Transactional
    public RepairCase create(Long serviceRequestId, Long technicianId) {
        return toDomain(jpa.save(new RepairCaseEntity(serviceRequestId, technicianId)), List.of());
    }

    @Override
    @Transactional
    public RepairCase updateStatus(Long id, RepairStatus status) {
        RepairCaseEntity entity = findEntityById(id);
        entity.updateStatus(status, null);
        return toDomain(jpa.save(entity), detailJpa.findByRepairCaseId(id));
    }

    @Override
    @Transactional
    public RepairCase updateStatusWithReason(Long id, RepairStatus status, String rejectReason) {
        RepairCaseEntity entity = findEntityById(id);
        entity.updateStatus(status, rejectReason);
        return toDomain(jpa.save(entity), detailJpa.findByRepairCaseId(id));
    }

    @Override
    @Transactional
    public RepairCase updateTotalAmount(Long id, BigDecimal totalAmount) {
        RepairCaseEntity entity = findEntityById(id);
        entity.updateTotalAmount(totalAmount);
        return toDomain(jpa.save(entity), detailJpa.findByRepairCaseId(id));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByServiceRequestId(Long serviceRequestId) {
        return jpa.existsByServiceRequestId(serviceRequestId);
    }

    private RepairCaseEntity findEntityById(Long id) {
        return jpa.findById(id).orElseThrow(
                () -> new BusinessException(ErrorCode.REPAIR_CASE_NOT_FOUND,
                        "존재하지 않는 수리 케이스입니다. id=" + id));
    }

    private List<RepairCase> toDomainsWithBatchDetails(List<RepairCaseEntity> cases) {
        if (cases.isEmpty()) return List.of();
        List<Long> ids = cases.stream().map(RepairCaseEntity::getId).toList();
        Map<Long, List<RepairDetailEntity>> detailsMap = detailJpa.findByRepairCaseIdIn(ids)
                .stream().collect(Collectors.groupingBy(RepairDetailEntity::getRepairCaseId));
        return cases.stream()
                .map(e -> toDomain(e, detailsMap.getOrDefault(e.getId(), List.of())))
                .toList();
    }

    private RepairCase toDomain(RepairCaseEntity e, List<RepairDetailEntity> details) {
        return new RepairCase(
                e.getId(), e.getServiceRequestId(), e.getTechnicianId(),
                e.getStatus(), e.getTotalAmount(), e.getRejectReason(),
                e.getCreatedAt(), e.getUpdatedAt(),
                details.stream().map(this::toDetailDomain).toList());
    }

    private RepairDetail toDetailDomain(RepairDetailEntity e) {
        return new RepairDetail(e.getId(), e.getRepairCaseId(),
                e.getDescription(), e.getAmount(), e.getCreatedAt());
    }
}
