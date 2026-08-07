package com.noomit.backend.reception.application;

import java.time.Instant;

public record ReassignServiceRequestCommand(long id, long technicianId, long slotId, Instant assignedAt, long version) {
}