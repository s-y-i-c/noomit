package com.noomit.backend.reception.application;

public record CancelServiceRequestCommand(long id, String cancelReason) {
}