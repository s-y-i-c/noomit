package com.noomit.backend.repair.application.port;

import com.noomit.backend.repair.domain.RepairCase;
import com.noomit.backend.repair.domain.RepairStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface RepairCaseRepository {
    Optional<RepairCase> findById(Long id);
    List<RepairCase> findByTechnicianId(Long technicianId);
    List<RepairCase> findAll();
    List<RepairCase> findAllByStatus(RepairStatus status);
    RepairCase create(Long serviceRequestId, Long technicianId);
    RepairCase updateStatus(Long id, RepairStatus status);
    RepairCase updateStatusWithReason(Long id, RepairStatus status, String rejectReason);
    RepairCase updateTotalAmount(Long id, BigDecimal totalAmount);
    boolean existsByServiceRequestId(Long serviceRequestId);
}
