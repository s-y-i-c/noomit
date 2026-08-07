package com.noomit.backend.repair.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

interface SpringDataRepairDetailRepository extends JpaRepository<RepairDetailEntity, Long> {
    List<RepairDetailEntity> findByRepairCaseId(Long repairCaseId);

    List<RepairDetailEntity> findByRepairCaseIdIn(List<Long> repairCaseIds);

    @Query("SELECT COALESCE(SUM(d.amount), 0) FROM RepairDetailEntity d WHERE d.repairCaseId = :repairCaseId")
    BigDecimal sumAmountByRepairCaseId(@Param("repairCaseId") Long repairCaseId);
}
