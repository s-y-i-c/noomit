package com.noomit.backend.reception.application;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import com.noomit.backend.reception.domain.ServiceRequest;

public interface ServiceRequestRepository {
    ServiceRequest create(long customerId, long productId, String symptom, String remarks, int baseFee, Instant requestedAt);

    Optional<ServiceRequest> findById(long id);

    /** RECEIVED 상태에서만 성공. 아니면 RECEPTION_INVALID_STATUS */
    void assignInitial(long id, long technicianId, long slotId, LocalDate visitDate,
                        LocalTime visitStartTime, LocalTime visitEndTime, Instant assignedAt);

    /** ASSIGNED 상태에서만 성공. 아니면 RECEPTION_INVALID_STATUS */
    void reassign(long id, long technicianId, long slotId, LocalDate visitDate,
                  LocalTime visitStartTime, LocalTime visitEndTime, Instant assignedAt, long version);

    /** 이미 CANCELLED면 RECEPTION_ALREADY_CANCELLED */
    void cancel(long id, String reason, Instant cancelledAt);
}