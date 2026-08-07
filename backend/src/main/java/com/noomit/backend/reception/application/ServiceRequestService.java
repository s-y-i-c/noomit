package com.noomit.backend.reception.application;

import java.time.Instant;
import com.noomit.backend.reception.ServiceRequestAssigned;
import com.noomit.backend.reception.domain.ServiceRequest;
import com.noomit.backend.reception.domain.ServiceRequestStatus;
import com.noomit.backend.reception.domain.TechnicianAvailability;
import com.noomit.backend.shared.error.BusinessException;
import com.noomit.backend.shared.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ServiceRequestService {
    private final ServiceRequestRepository serviceRequests;
    private final TechnicianAvailabilityRepository technicianAvailabilities;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public ServiceRequest assignInitial(long id, long technicianId, long slotId, Instant assignedAt) {
        ServiceRequest request = findRequest(id);
        if (request.status() != ServiceRequestStatus.RECEIVED) {
            throw new BusinessException(ErrorCode.RECEPTION_INVALID_STATUS, "접수 대기 상태에서만 배정할 수 있습니다.");
        }
        TechnicianAvailability slot = findSlot(slotId);
        validateSlotOwnership(slot, technicianId);

        technicianAvailabilities.occupySlot(slotId);
        serviceRequests.assignInitial(id, technicianId, slotId, slot.availableDate(), slot.startTime(), slot.endTime(), assignedAt);

        publishServiceRequestAssigned(request, technicianId, assignedAt);
        return findRequest(id);
    }

    @Transactional
    public ServiceRequest reassign(long id, long technicianId, long slotId, Instant assignedAt, long version) {
        ServiceRequest request = findRequest(id);
        if (request.status() != ServiceRequestStatus.ASSIGNED) {
            throw new BusinessException(ErrorCode.RECEPTION_INVALID_STATUS, "배정된 접수만 재배정할 수 있습니다.");
        }
        TechnicianAvailability slot = findSlot(slotId);
        validateSlotOwnership(slot, technicianId);

        if (request.reservedSlotId() != null) {
            technicianAvailabilities.releaseSlot(request.reservedSlotId());
        }
        technicianAvailabilities.occupySlot(slotId);
        serviceRequests.reassign(id, technicianId, slotId, slot.availableDate(), slot.startTime(), slot.endTime(), assignedAt, version);

        publishServiceRequestAssigned(request, technicianId, assignedAt);
        return findRequest(id);
    }

    private void publishServiceRequestAssigned(ServiceRequest request, long technicianId, Instant assignedAt) {
        eventPublisher.publishEvent(new ServiceRequestAssigned(
                request.id(), request.customerId(), request.productId(), technicianId, assignedAt));
    }

    private ServiceRequest findRequest(long id) {
        return serviceRequests.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RECEPTION_INVALID_REQUEST, "접수를 찾을 수 없습니다."));
    }

    private TechnicianAvailability findSlot(long slotId) {
        return technicianAvailabilities.findById(slotId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RECEPTION_INVALID_REQUEST, "슬롯을 찾을 수 없습니다."));
    }

    private void validateSlotOwnership(TechnicianAvailability slot, long technicianId) {
        if (slot.technicianId() != technicianId) {
            throw new BusinessException(ErrorCode.RECEPTION_INVALID_REQUEST, "슬롯이 해당 기사의 것이 아닙니다.");
        }
    }
}