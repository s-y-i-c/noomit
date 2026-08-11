package com.noomit.backend.reception.application;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import com.noomit.backend.reception.ServiceRequestAssigned;
import com.noomit.backend.reception.domain.ServiceRequest;
import com.noomit.backend.reception.domain.ServiceRequestStatus;
import com.noomit.backend.reception.domain.TechnicianAvailability;
import com.noomit.backend.shared.error.BusinessException;
import com.noomit.backend.shared.error.ErrorCode;
import com.noomit.backend.user.UserDirectory;
import com.noomit.backend.user.UserRef;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ServiceRequestService {
    /** MVP 고정 출장비. 책정 정책은 추후 도입. */
    private static final int DEFAULT_BASE_FEE = 20000;
    private static final int MAX_CANCEL_REASON_LENGTH = 300;

    private final ServiceRequestRepository requestRepository;
    private final TechnicianAvailabilityRepository availabilityRepository;
    private final UserDirectory userDirectory;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    public int getBaseFeePolicy() {
        return DEFAULT_BASE_FEE;
    }

    @Transactional
    public ServiceRequest create(CreateServiceRequestCommand command) {
        if (command.symptom() == null || command.symptom().isBlank()) {
            throw new BusinessException(ErrorCode.RECEPTION_INVALID_REQUEST, "고장 증상 내용이 필요합니다.");
        }
        validateProductSource(command.productId(), command.selectedSubCategoryId(), command.selectedModelName());
        ServiceRequest request = ServiceRequest.create(command.customerId(), command.productId(),
                command.selectedSubCategoryId(), command.selectedModelName(), command.receptionistId(),
                command.symptom(), command.remarks(), DEFAULT_BASE_FEE, Instant.now(clock));
        return requestRepository.create(request);
    }

    // 모델 코드를 알면 productId만, 모르면 세부카테고리+모델명만 — 둘 다 있거나 둘 다 없으면 안 됨 (생성/수정 공통)
    private void validateProductSource(Long productId, Long selectedSubCategoryId, String selectedModelName) {
        boolean hasProduct = productId != null;
        boolean hasSelectedInfo = selectedSubCategoryId != null
                && selectedModelName != null
                && !selectedModelName.isBlank();

        if (hasProduct == hasSelectedInfo) {
            throw new BusinessException(ErrorCode.RECEPTION_INVALID_REQUEST,
                    "제품은 모델 코드 또는 서브카테고리+모델명 중 하나로만 입력해야 합니다.");
        }
    }

    @Transactional
    public AssignmentResult assignInitial(AssignServiceRequestCommand command) {
        long id = command.id();
        long technicianId = command.technicianId();
        long slotId = command.slotId();
        Instant assignedAt = Instant.now(clock);

        ServiceRequest request = findRequest(id);
        if (!request.canAssign()) {
            throw new BusinessException(ErrorCode.RECEPTION_INVALID_STATUS, "접수 대기 상태에서만 배정할 수 있습니다.");
        }
        TechnicianAvailability slot = findSlot(slotId);
        if (!slot.isOwnedBy(technicianId)) {
            throw new BusinessException(ErrorCode.RECEPTION_SLOT_NOT_OWNED, "슬롯이 해당 기사의 것이 아닙니다.");
        }

        availabilityRepository.occupySlot(slotId);
        int updated = requestRepository.assignInitial(id, technicianId, slotId, slot.availableDate(), slot.startTime(), slot.endTime(), assignedAt);
        if (updated == 0) {
            throw new BusinessException(ErrorCode.RECEPTION_INVALID_STATUS, "접수 대기 상태에서만 배정할 수 있습니다.");
        }

        publishServiceRequestAssigned(request, technicianId, assignedAt);
        return AssignmentResult.of(id, ServiceRequestStatus.ASSIGNED, findTechnician(technicianId).name(), slot);
    }

    @Transactional
    public AssignmentResult reassign(ReassignServiceRequestCommand command) {
        long id = command.id();
        long technicianId = command.technicianId();
        long slotId = command.slotId();
        Instant assignedAt = Instant.now(clock);
        long version = command.version();

        ServiceRequest request = findRequest(id);
        // 동시성 제어: 슬롯 변경 전에 version을 먼저 검증하여 이미 변경된 요청에 대한 불필요한 슬롯 UPDATE를 방지한다.
        // 최종 검증은 UPDATE 시 WHERE version 조건으로 보장한다.
        if (request.version() != version) {
            throw new BusinessException(ErrorCode.RECEPTION_CONCURRENT_MODIFICATION, "이미 변경된 접수입니다. 새로고침 후 다시 시도해주세요.");
        }
        if (!request.canReassign()) {
            throw new BusinessException(ErrorCode.RECEPTION_INVALID_STATUS, "배정된 접수만 재배정할 수 있습니다.");
        }
        TechnicianAvailability slot = findSlot(slotId);
        if (!slot.isOwnedBy(technicianId)) {
            throw new BusinessException(ErrorCode.RECEPTION_SLOT_NOT_OWNED, "슬롯이 해당 기사의 것이 아닙니다.");
        }

        Long previousSlotId = request.reservedSlotId();
        boolean slotChanged = previousSlotId == null || previousSlotId != slotId;
        if (slotChanged) {
            availabilityRepository.occupySlot(slotId);
            if (previousSlotId != null) {
                availabilityRepository.releaseSlot(previousSlotId);
            }
        }
        int updated = requestRepository.reassign(id, technicianId, slotId, slot.availableDate(), slot.startTime(), slot.endTime(), assignedAt, version);
        if (updated == 0) {
            throw new BusinessException(ErrorCode.RECEPTION_CONCURRENT_MODIFICATION, "이미 변경된 접수입니다. 새로고침 후 다시 시도해주세요.");
        }

        publishServiceRequestAssigned(request, technicianId, assignedAt);
        return AssignmentResult.of(id, ServiceRequestStatus.ASSIGNED, findTechnician(technicianId).name(), slot);
    }

    @Transactional
    public CancellationResult cancel(CancelServiceRequestCommand command) {
        long id = command.id();
        String reason = command.cancelReason();
        Instant cancelledAt = Instant.now(clock);

        if (reason != null && reason.length() > MAX_CANCEL_REASON_LENGTH) {
            throw new BusinessException(ErrorCode.RECEPTION_INVALID_REQUEST, "취소 사유는 300자 이하로 입력해주세요.");
        }

        ServiceRequest request = findRequest(id);
        if (!request.canCancel()) {
            throw new BusinessException(ErrorCode.RECEPTION_ALREADY_CANCELLED, "이미 취소되었거나 취소할 수 없는 접수입니다.");
        }

        int updated = requestRepository.cancel(id, reason, cancelledAt);
        if (updated == 0) {
            throw new BusinessException(ErrorCode.RECEPTION_ALREADY_CANCELLED, "이미 취소되었거나 취소할 수 없는 접수입니다.");
        }

        Long reservedSlotId = request.reservedSlotId();
        if (reservedSlotId != null) {
            availabilityRepository.releaseSlot(reservedSlotId);
        }

        return new CancellationResult(id, ServiceRequestStatus.CANCELLED, reason, cancelledAt);
    }

    @Transactional
    public ServiceRequest update(UpdateServiceRequestCommand command) {
        if (command.symptom() == null || command.symptom().isBlank()) {
            throw new BusinessException(ErrorCode.RECEPTION_INVALID_REQUEST, "고장 증상 내용이 필요합니다.");
        }
        validateProductSource(command.productId(), command.selectedSubCategoryId(), command.selectedModelName());

        ServiceRequest request = findRequest(command.id());
        // 동시성 제어
        if (request.version() != command.version()) {
            throw new BusinessException(ErrorCode.RECEPTION_CONCURRENT_MODIFICATION, "이미 변경된 접수입니다. 새로고침 후 다시 시도해주세요.");
        }
        if (!request.canEdit()) {
            throw new BusinessException(ErrorCode.RECEPTION_INVALID_STATUS, "취소된 접수는 수정할 수 없습니다.");
        }

        int updated = requestRepository.update(command.id(), command.customerId(), command.productId(),
                command.selectedSubCategoryId(), command.selectedModelName(), command.symptom(), command.remarks(),
                command.version());
        if (updated == 0) {
            throw new BusinessException(ErrorCode.RECEPTION_CONCURRENT_MODIFICATION, "이미 변경된 접수입니다. 새로고침 후 다시 시도해주세요.");
        }

        return findRequest(command.id());
    }

    private UserRef findTechnician(long technicianId) {
        return userDirectory.findActiveByIds(List.of(technicianId)).stream()
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.RECEPTION_NOT_FOUND, "기사를 찾을 수 없습니다."));
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
}