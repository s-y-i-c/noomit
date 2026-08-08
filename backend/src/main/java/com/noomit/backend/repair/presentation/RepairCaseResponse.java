package com.noomit.backend.repair.presentation;

import com.noomit.backend.repair.domain.RepairCase;
import com.noomit.backend.repair.domain.RepairDetail;
import com.noomit.backend.repair.domain.RepairStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

record RepairCaseResponse(
        String id,
        String serviceRequestId,
        String technicianId,
        RepairStatus status,
        BigDecimal totalAmount,
        String rejectReason,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        List<RepairDetailResponse> details
) {
    record RepairDetailResponse(
            String id,
            String repairCaseId,
            String description,
            BigDecimal amount,
            OffsetDateTime createdAt
    ) {
        static RepairDetailResponse from(RepairDetail detail) {
            return new RepairDetailResponse(
                    Long.toString(detail.id()),
                    Long.toString(detail.repairCaseId()),
                    detail.description(),
                    detail.amount(),
                    detail.createdAt()
            );
        }
    }

    static RepairCaseResponse from(RepairCase repairCase) {
        return new RepairCaseResponse(
                Long.toString(repairCase.id()),
                Long.toString(repairCase.serviceRequestId()),
                Long.toString(repairCase.technicianId()),
                repairCase.status(),
                repairCase.totalAmount(),
                repairCase.rejectReason(),
                repairCase.createdAt(),
                repairCase.updatedAt(),
                repairCase.details().stream().map(RepairDetailResponse::from).toList()
        );
    }
}
