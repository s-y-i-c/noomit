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
    private final ServiceRequestRepository requestRepository;
    private final TechnicianAvailabilityRepository availabilityRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public ServiceRequest assignInitial(AssignServiceRequestCommand command) {
        long id = command.id();
        long technicianId = command.technicianId();
        long slotId = command.slotId();
        Instant assignedAt = command.assignedAt();

        ServiceRequest request = findRequest(id);
        if (request.status() != ServiceRequestStatus.RECEIVED) {
            throw new BusinessException(ErrorCode.RECEPTION_INVALID_STATUS, "접수 대기 상태에서만 배정할 수 있습니다.");
        }
        TechnicianAvailability slot = findSlot(slotId);
        validateSlotOwnership(slot, technicianId);

        availabilityRepository.occupySlot(slotId);
        requestRepository.assignInitial(id, technicianId, slotId, slot.availableDate(), slot.startTime(), slot.endTime(), assignedAt);

        publishServiceRequestAssigned(request, technicianId, assignedAt);
        return findRequest(id);
    }

    @Transactional
    public ServiceRequest reassign(ReassignServiceRequestCommand command) {
        long id = command.id();
        long technicianId = command.technicianId();
        long slotId = command.slotId();
        Instant assignedAt = command.assignedAt();
        long version = command.version();

        ServiceRequest request = findRequest(id);
        // 동시성 제어: 슬롯 변경 전에 version을 먼저 검증하여 이미 변경된 요청에 대한 불필요한 슬롯 UPDATE를 방지한다.
        // 최종 검증은 UPDATE 시 WHERE version 조건으로 보장한다.
        if (request.version() != version) {
            throw new BusinessException(ErrorCode.RECEPTION_CONCURRENT_MODIFICATION, "이미 변경된 접수입니다. 새로고침 후 다시 시도해주세요.");
        }
        if (request.status() != ServiceRequestStatus.ASSIGNED) {
            throw new BusinessException(ErrorCode.RECEPTION_INVALID_STATUS, "배정된 접수만 재배정할 수 있습니다.");
        }
        TechnicianAvailability slot = findSlot(slotId);
        validateSlotOwnership(slot, technicianId);

        Long previousSlotId = request.reservedSlotId();
        boolean slotChanged = previousSlotId == null || previousSlotId != slotId;
        if (slotChanged) {
            availabilityRepository.occupySlot(slotId);
            if (previousSlotId != null) {
                availabilityRepository.releaseSlot(previousSlotId);
            }
        }
        requestRepository.reassign(id, technicianId, slotId, slot.availableDate(), slot.startTime(), slot.endTime(), assignedAt, version);

        publishServiceRequestAssigned(request, technicianId, assignedAt);
        return findRequest(id);
    }

    private void publishServiceRequestAssigned(ServiceRequest request, long technicianId, Instant assignedAt) {
        eventPublisher.publishEvent(new ServiceRequestAssigned(
                request.id(), request.customerId(), request.productId(), technicianId, assignedAt));
    }

    private ServiceRequest findRequest(long id) {
        return requestRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RECEPTION_NOT_FOUND, "접수를 찾을 수 없습니다."));
    }

    private TechnicianAvailability findSlot(long slotId) {
        return availabilityRepository.findById(slotId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RECEPTION_NOT_FOUND, "슬롯을 찾을 수 없습니다."));
    }

    private void validateSlotOwnership(TechnicianAvailability slot, long technicianId) {
        if (slot.technicianId() != technicianId) {
            throw new BusinessException(ErrorCode.RECEPTION_SLOT_NOT_OWNED, "슬롯이 해당 기사의 것이 아닙니다.");
        }
    }
}