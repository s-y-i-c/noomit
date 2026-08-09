package com.noomit.backend.reception.application;

public record ReassignServiceRequestCommand(long id, long technicianId, long slotId, long version) {
}