package com.noomit.backend.reception.application;

public record AssignServiceRequestCommand(long id, long technicianId, long slotId) {
}