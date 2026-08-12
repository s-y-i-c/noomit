package com.noomit.backend.reception.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

public record ServiceRequest(
        Long id,
        String requestNumber,
        long customerId,
        Long productId,
        Long selectedSubCategoryId,
        String selectedModelName,
        long receptionistId,
        String symptom,
        String remarks,
        int baseFee,
        Long reservedSlotId,
        Long technicianId,
        LocalDate visitDate,
        LocalTime visitStartTime,
        LocalTime visitEndTime,
        ServiceRequestStatus status,
        Instant requestedAt,
        Instant assignedAt,
        Instant cancelledAt,
        String cancelReason,
        long version,
        Instant createdAt,
        Instant updatedAt) {

    public static ServiceRequest create(String requestNumber, long customerId, Long productId,
                                         Long selectedSubCategoryId, String selectedModelName, long receptionistId,
                                         String symptom, String remarks, int baseFee, Instant requestedAt) {
        return new ServiceRequest(
                null, requestNumber, customerId, productId, selectedSubCategoryId, selectedModelName,
                receptionistId, symptom, remarks, baseFee,
                null, null, null, null, null,
                ServiceRequestStatus.RECEIVED,
                requestedAt, null, null, null,
                0L, requestedAt, requestedAt
        );
    }

    public boolean canAssign() {
        return status == ServiceRequestStatus.RECEIVED;
    }

    public boolean canReassign() {
        return status == ServiceRequestStatus.ASSIGNED;
    }

    public boolean canCancel() {
        return status == ServiceRequestStatus.RECEIVED || status == ServiceRequestStatus.ASSIGNED;
    }

    public boolean canEdit() {
        return status == ServiceRequestStatus.RECEIVED || status == ServiceRequestStatus.ASSIGNED;
    }
}