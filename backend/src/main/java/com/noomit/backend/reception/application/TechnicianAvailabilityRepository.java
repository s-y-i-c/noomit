package com.noomit.backend.reception.application;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import com.noomit.backend.reception.domain.TechnicianAvailability;
import com.noomit.backend.reception.domain.TechnicianAvailabilityStatus;

public interface TechnicianAvailabilityRepository {
    TechnicianAvailability create(long technicianId, LocalDate availableDate, LocalTime startTime, LocalTime endTime);

    Optional<TechnicianAvailability> findById(long id);

    /** AVAILABLE 상태에서만 성공. 아니면 RECEPTION_INVALID_STATUS */
    void occupySlot(long id);

    /** UNAVAILABLE 상태에서만 성공. 아니면 RECEPTION_INVALID_STATUS */
    void releaseSlot(long id);

    /** 본인 슬롯 + 배정된(ASSIGNED) 접수가 없을 때만 성공. */
    int updateAvailabilityStatus(long id, long technicianId, TechnicianAvailabilityStatus status);

    /** 이미 있는 (technician_id, date, start_time, end_time)는 건드리지 않고 없는 것만 AVAILABLE로 삽입 */
    void insertAllIfAbsent(List<AvailabilitySlot> slots);

    /** from(포함) 이후, 배정된 접수가 없는 슬롯만 삭제. 삭제된 행 수를 반환한다. */
    int deleteUnassignedFutureSlots(long technicianId, LocalDate from);
}