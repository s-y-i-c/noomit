package com.noomit.backend.reception.application;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import com.noomit.backend.reception.domain.ServiceRequest;

public interface ServiceRequestRepository {
    ServiceRequest create(ServiceRequest request);

    Optional<ServiceRequest> findById(long id);

    /** RECEIVED 상태에서만 성공. */
    int assignInitial(long id, long technicianId, long slotId, LocalDate visitDate,
                       LocalTime visitStartTime, LocalTime visitEndTime, Instant assignedAt);

    /** ASSIGNED 상태 + version 일치할 때만 성공. */
    int reassign(long id, long technicianId, long slotId, LocalDate visitDate,
                 LocalTime visitStartTime, LocalTime visitEndTime, Instant assignedAt, long version);

    /** RECEIVED/ASSIGNED 상태에서만 성공. */
    int cancel(long id, String reason, Instant cancelledAt);
}