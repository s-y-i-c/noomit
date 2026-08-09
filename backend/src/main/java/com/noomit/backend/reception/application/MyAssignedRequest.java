package com.noomit.backend.reception.application;

import java.time.LocalTime;

public record MyAssignedRequest(
        long serviceRequestId,
        String customerName,
        String address,
        String modelName,
        LocalTime startTime,
        LocalTime endTime) {
}