package com.noomit.backend.reception.application;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import com.noomit.backend.reception.domain.TechnicianAvailability;

public interface TechnicianAvailabilityRepository {
    TechnicianAvailability create(long technicianId, LocalDate availableDate, LocalTime startTime, LocalTime endTime);

    Optional<TechnicianAvailability> findById(long id);

    /** AVAILABLE 상태에서만 성공. 아니면 RECEPTION_INVALID_STATUS */
    void occupySlot(long id);

    /** UNAVAILABLE 상태에서만 성공. 아니면 RECEPTION_INVALID_STATUS */
    void releaseSlot(long id);

    /** AVAILABLE <-> UNAVAILABLE. 본인 슬롯 아니면 RECEPTION_INVALID_REQUEST */
    void toggleAvailability(long id, long technicianId);
}