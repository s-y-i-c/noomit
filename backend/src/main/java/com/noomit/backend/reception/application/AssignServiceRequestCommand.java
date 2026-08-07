package com.noomit.backend.reception.application;

import java.time.Instant;

public record AssignServiceRequestCommand(long id, long technicianId, long slotId, Instant assignedAt) {
}