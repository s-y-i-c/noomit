package com.noomit.backend.reception.infrastructure.persistence;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import com.noomit.backend.reception.application.ServiceRequestRepository;
import com.noomit.backend.reception.domain.ServiceRequest;
import com.noomit.backend.shared.error.BusinessException;
import com.noomit.backend.shared.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
class ServiceRequestJpaAdapter implements ServiceRequestRepository {
    private final ServiceRequestJpaRepository requestJpaRepository;

    @Override
    public ServiceRequest create(long customerId, long productId, String symptom, String remarks, int baseFee, Instant requestedAt) {
        ServiceRequestEntity entity = new ServiceRequestEntity(customerId, productId, symptom, remarks, baseFee, requestedAt);
        return requestJpaRepository.save(entity).toDomain();
    }

    @Override
    public Optional<ServiceRequest> findById(long id) {
        return requestJpaRepository.findById(id).map(ServiceRequestEntity::toDomain);
    }

    @Override
    public void assignInitial(long id, long technicianId, long slotId, LocalDate visitDate,
                               LocalTime visitStartTime, LocalTime visitEndTime, Instant assignedAt) {
        int updated = requestJpaRepository.assign(id, technicianId, slotId, visitDate, visitStartTime, visitEndTime,
                assignedAt);
        if (updated == 0) {
            throw new BusinessException(ErrorCode.RECEPTION_INVALID_STATUS, "접수 대기 상태에서만 배정할 수 있습니다.");
        }
    }

    @Override
    public void reassign(long id, long technicianId, long slotId, LocalDate visitDate,
                          LocalTime visitStartTime, LocalTime visitEndTime, Instant assignedAt, long version) {
        int updated = requestJpaRepository.reassign(id, technicianId, slotId, visitDate, visitStartTime, visitEndTime,
                assignedAt, version);
        if (updated == 0) {
            throw new BusinessException(ErrorCode.RECEPTION_INVALID_STATUS, "배정된 접수만 재배정할 수 있습니다.");
        }
    }

    @Override
    public void cancel(long id, String reason, Instant cancelledAt) {
        int updated = requestJpaRepository.cancel(id, reason, cancelledAt);
        if (updated == 0) {
            throw new BusinessException(ErrorCode.RECEPTION_ALREADY_CANCELLED, "이미 취소되었거나 취소할 수 없는 접수입니다.");
        }
    }
}