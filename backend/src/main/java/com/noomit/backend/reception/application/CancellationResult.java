package com.noomit.backend.reception.application;

import java.time.Instant;
import com.noomit.backend.reception.domain.ServiceRequestStatus;

public record CancellationResult(long id, ServiceRequestStatus status, String cancelReason, Instant cancelledAt) {
}