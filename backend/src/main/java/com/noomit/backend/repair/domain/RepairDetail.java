package com.noomit.backend.repair.domain;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record RepairDetail(
        Long id,
        Long repairCaseId,
        String description,
        BigDecimal amount,
        OffsetDateTime createdAt
) {}
