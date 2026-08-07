package com.noomit.backend.repair.infrastructure.persistence;

import com.noomit.backend.repair.domain.RepairStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface SpringDataRepairCaseRepository extends JpaRepository<RepairCaseEntity, Long> {
    List<RepairCaseEntity> findByTechnicianId(Long technicianId);
    List<RepairCaseEntity> findByStatus(RepairStatus status);
    boolean existsByServiceRequestId(Long serviceRequestId);
}
