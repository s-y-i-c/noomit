package com.noomit.backend.reception.application;

import java.time.LocalDate;
import java.time.LocalTime;

public record MyAssignedRequestDetail(
        long serviceRequestId,
        String requestNumber,
        String customerName,
        String customerPhone,
        String address,
        String detailAddress,
        String modelName,
        String symptom,
        String remarks,
        LocalDate visitDate,
        LocalTime startTime,
        LocalTime endTime) {
}