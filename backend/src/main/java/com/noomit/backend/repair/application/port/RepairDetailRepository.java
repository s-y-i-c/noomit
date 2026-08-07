package com.noomit.backend.repair.application.port;

import com.noomit.backend.repair.domain.RepairDetail;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface RepairDetailRepository {
    RepairDetail save(Long repairCaseId, String description, BigDecimal amount);
    void deleteById(Long id);
    Optional<RepairDetail> findById(Long id);
    List<RepairDetail> findByRepairCaseId(Long repairCaseId);
    BigDecimal sumAmountByRepairCaseId(Long repairCaseId);
}
