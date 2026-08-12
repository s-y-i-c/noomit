package com.noomit.backend.reception.infrastructure.persistence;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
    private static final ZoneId REQUEST_NUMBER_ZONE = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter REQUEST_NUMBER_DATE_FORMAT = DateTimeFormatter.ofPattern("yyMMdd");

    private final ServiceRequestJpaRepository requestJpaRepository;
    private final ServiceRequestNumberCounterJpaRepository requestNumberCounterJpaRepository;

    @Override
    public ServiceRequest create(ServiceRequest request) {
        String requestNumber = issueRequestNumber(request.requestedAt());
        ServiceRequestEntity entity = new ServiceRequestEntity(request.customerId(), request.productId(),
                request.selectedSubCategoryId(), request.selectedModelName(), request.receptionistId(), requestNumber,
                request.symptom(), request.remarks(), request.baseFee(), request.requestedAt());
        return requestJpaRepository.save(entity).toDomain();
    }

    // 날짜별 카운터를 원자적으로 증가시켜 "RCP-yyMMdd-순번" 형식의 접수번호 생성 
    private String issueRequestNumber(Instant requestedAt) {
        LocalDate requestDate = requestedAt.atZone(REQUEST_NUMBER_ZONE).toLocalDate();
        int seq = requestNumberCounterJpaRepository.incrementAndGet(requestDate);
        return "RCP-" + requestDate.format(REQUEST_NUMBER_DATE_FORMAT) + "-" + String.format("%04d", seq);
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