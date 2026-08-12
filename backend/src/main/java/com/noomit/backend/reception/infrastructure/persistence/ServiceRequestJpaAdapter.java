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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
class ServiceRequestJpaAdapter implements ServiceRequestRepository {
    private final ServiceRequestJpaRepository requestJpaRepository;

    @Override
    public ServiceRequest create(ServiceRequest request) {
        ServiceRequestEntity entity = new ServiceRequestEntity(request.customerId(), request.productId(),
                request.selectedSubCategoryId(), request.selectedModelName(), request.receptionistId(),
                request.requestNumber(), request.symptom(), request.remarks(), request.baseFee(),
                request.requestedAt());
        return requestJpaRepository.save(entity).toDomain();
    }

    @Override
    public Optional<ServiceRequest> findById(long id) {
        return requestJpaRepository.findById(id).map(ServiceRequestEntity::toDomain);
    }

    @Override
    public int assignInitial(long id, long technicianId, long slotId, LocalDate visitDate,
                              LocalTime visitStartTime, LocalTime visitEndTime, Instant assignedAt) {
        try {
            return requestJpaRepository.assignInitial(id, technicianId, slotId, visitDate, visitStartTime, visitEndTime, assignedAt);
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessException(ErrorCode.RECEPTION_SLOT_ALREADY_BOOKED, "이미 예약된 슬롯입니다.");
        }
    }

    @Override
    public int reassign(long id, long technicianId, long slotId, LocalDate visitDate,
                         LocalTime visitStartTime, LocalTime visitEndTime, Instant assignedAt, long version) {
        try {
            return requestJpaRepository.reassign(id, technicianId, slotId, visitDate, visitStartTime, visitEndTime,
                    assignedAt, version);
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessException(ErrorCode.RECEPTION_SLOT_ALREADY_BOOKED, "이미 예약된 슬롯입니다.");
        }
    }

    @Override
    public int cancel(long id, String reason, Instant cancelledAt) {
        return requestJpaRepository.cancel(id, reason, cancelledAt);
    }

    @Override
    public int update(long id, long customerId, Long productId, Long selectedSubCategoryId,
                      String selectedModelName, String symptom, String remarks, long version) {
        return requestJpaRepository.update(id, customerId, productId, selectedSubCategoryId, 
                selectedModelName, symptom, remarks, version);
    }
}