package com.noomit.backend.repair.application;

import com.noomit.backend.reception.ServiceRequestAssigned;
import com.noomit.backend.repair.application.port.RepairCaseRepository;
import com.noomit.backend.repair.application.port.RepairDetailRepository;
import com.noomit.backend.repair.domain.RepairCase;
import com.noomit.backend.repair.domain.RepairDetail;
import com.noomit.backend.repair.domain.RepairStatus;
import com.noomit.backend.shared.error.BusinessException;
import com.noomit.backend.shared.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RepairCaseService {

    private final RepairCaseRepository repairCaseRepository;
    private final RepairDetailRepository repairDetailRepository;

    @ApplicationModuleListener
    public void onServiceRequestAssigned(ServiceRequestAssigned event) {
        if (repairCaseRepository.existsByServiceRequestId(event.serviceRequestId())) {
            return; // 멱등성: 이미 케이스가 생성된 경우 무시
        }
        repairCaseRepository.create(event.serviceRequestId(), event.technicianId());
    }

    // ── 기사용 ──────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<RepairCase> getMyRepairCases(long technicianId) {
        return repairCaseRepository.findByTechnicianId(technicianId);
    }

    @Transactional(readOnly = true)
    public RepairCase getRepairCase(long id, long technicianId) {
        RepairCase repairCase = findRepairCase(id);
        validateOwnership(repairCase, technicianId);
        return repairCase;
    }

    @Transactional
    public RepairCase addDetail(long caseId, long technicianId, String description, BigDecimal amount) {
        RepairCase repairCase = findRepairCase(caseId);
        validateOwnership(repairCase, technicianId);
        validateStatus(repairCase, RepairStatus.IN_PROGRESS, "진행 중 상태에서만 수리내역을 추가할 수 있습니다.");

        repairDetailRepository.save(caseId, description, amount);
        BigDecimal totalAmount = repairDetailRepository.sumAmountByRepairCaseId(caseId);
        return repairCaseRepository.updateTotalAmount(caseId, totalAmount);
    }

    @Transactional
    public void deleteDetail(long caseId, long detailId, long technicianId) {
        RepairCase repairCase = findRepairCase(caseId);
        validateOwnership(repairCase, technicianId);
        validateStatus(repairCase, RepairStatus.IN_PROGRESS, "진행 중 상태에서만 수리내역을 삭제할 수 있습니다.");

        RepairDetail detail = repairDetailRepository.findById(detailId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REPAIR_DETAIL_NOT_FOUND,
                        "존재하지 않는 수리내역입니다. id=" + detailId));
        if (!detail.repairCaseId().equals(caseId)) {
            throw new BusinessException(ErrorCode.REPAIR_DETAIL_NOT_IN_CASE,
                    "해당 수리 케이스의 내역이 아닙니다. detailId=" + detailId);
        }

        repairDetailRepository.deleteById(detailId);
        BigDecimal totalAmount = repairDetailRepository.sumAmountByRepairCaseId(caseId);
        repairCaseRepository.updateTotalAmount(caseId, totalAmount);
    }

    @Transactional
    public RepairCase submit(long caseId, long technicianId) {
        RepairCase repairCase = findRepairCase(caseId);
        validateOwnership(repairCase, technicianId);
        validateStatus(repairCase, RepairStatus.IN_PROGRESS, "진행 중 상태에서만 제출할 수 있습니다.");
        return repairCaseRepository.updateStatus(caseId, RepairStatus.SUBMITTED);
    }

    // ── 관리자용 ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<RepairCase> getAllRepairCases(RepairStatus status) {
        if (status == null) {
            return repairCaseRepository.findAll();
        }
        return repairCaseRepository.findAllByStatus(status);
    }

    @Transactional
    public RepairCase approve(long caseId) {
        RepairCase repairCase = findRepairCase(caseId);
        validateStatus(repairCase, RepairStatus.SUBMITTED, "제출된 상태에서만 승인할 수 있습니다.");
        return repairCaseRepository.updateStatus(caseId, RepairStatus.COMPLETED);
    }

    @Transactional
    public RepairCase reject(long caseId, String reason) {
        RepairCase repairCase = findRepairCase(caseId);
        validateStatus(repairCase, RepairStatus.SUBMITTED, "제출된 상태에서만 반려할 수 있습니다.");
        return repairCaseRepository.updateStatusWithReason(caseId, RepairStatus.IN_PROGRESS, reason);
    }

    // ── 내부 헬퍼 ────────────────────────────────────────────────────────────

    private RepairCase findRepairCase(long id) {
        return repairCaseRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.REPAIR_CASE_NOT_FOUND,
                        "존재하지 않는 수리 케이스입니다. id=" + id));
    }

    private void validateOwnership(RepairCase repairCase, long technicianId) {
        if (!repairCase.technicianId().equals(technicianId)) {
            throw new BusinessException(ErrorCode.REPAIR_CASE_ACCESS_DENIED,
                    "본인의 수리 케이스만 접근할 수 있습니다. id=" + repairCase.id());
        }
    }

    private void validateStatus(RepairCase repairCase, RepairStatus expected, String message) {
        if (repairCase.status() != expected) {
            throw new BusinessException(ErrorCode.REPAIR_CASE_INVALID_STATUS, message);
        }
    }
}
