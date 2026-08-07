package com.noomit.backend.repair.domain;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record RepairCase(
        Long id,
        Long serviceRequestId,
        Long technicianId,
        RepairStatus status,
        BigDecimal totalAmount,
        String rejectReason,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        List<RepairDetail> details
) {}
