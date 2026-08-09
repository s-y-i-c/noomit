package com.noomit.backend.reception.presentation;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import com.noomit.backend.reception.application.MyAssignedRequest;
import com.noomit.backend.reception.application.MyAssignedRequestDetail;
import com.noomit.backend.reception.application.MyAvailabilitySlot;
import com.noomit.backend.reception.application.ServiceRequestQueryService;
import com.noomit.backend.reception.application.TechnicianAvailabilityService;
import com.noomit.backend.reception.domain.TechnicianAvailabilityStatus;
import com.noomit.backend.shared.ApiResponse;
import com.noomit.backend.shared.error.BusinessException;
import com.noomit.backend.shared.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/engineer/me")
@RequiredArgsConstructor
class TechnicianController {
    private final ServiceRequestQueryService serviceRequestQueryService;
    private final TechnicianAvailabilityService technicianAvailabilityService;

    @GetMapping("/requests")
    ApiResponse<List<MyAssignedRequestResponse>> getMyRequests(
            @AuthenticationPrincipal(expression = "userId") long technicianId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<MyAssignedRequest> requests = serviceRequestQueryService.getMyAssignedRequests(technicianId, date);
        return ApiResponse.success(requests.stream()
                .map(MyAssignedRequestResponse::from)
                .toList());
    }

    @GetMapping("/requests/{requestId}")
    ApiResponse<MyAssignedRequestDetailResponse> getMyRequestDetail(
            @AuthenticationPrincipal(expression = "userId") long technicianId,
            @PathVariable String requestId) {
        MyAssignedRequestDetail detail = serviceRequestQueryService.getMyAssignedRequestDetail(technicianId, parseId(requestId));
        return ApiResponse.success(MyAssignedRequestDetailResponse.from(detail));
    }

    @GetMapping("/availability")
    ApiResponse<List<MyAvailabilitySlotResponse>> getMyAvailability(
            @AuthenticationPrincipal(expression = "userId") long technicianId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<MyAvailabilitySlot> slots = technicianAvailabilityService.getMyAvailabilitySlots(technicianId, date);
        return ApiResponse.success(slots.stream()
                .map(MyAvailabilitySlotResponse::from)
                .toList());
    }

    @PatchMapping("/availability/{slotId}")
    ApiResponse<MyAvailabilitySlotResponse> updateMyAvailability(
            @AuthenticationPrincipal(expression = "userId") long technicianId,
            @PathVariable String slotId,
            @RequestBody UpdateAvailabilityRequest request) {
        MyAvailabilitySlot slot = technicianAvailabilityService.updateAvailabilityStatus(
                technicianId, parseId(slotId), parseStatus(request.status()));
        return ApiResponse.success(MyAvailabilitySlotResponse.from(slot));
    }

    private TechnicianAvailabilityStatus parseStatus(String value) {
        try {
            return TechnicianAvailabilityStatus.valueOf(value);
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new BusinessException(ErrorCode.RECEPTION_INVALID_REQUEST, "허용되지 않은 status 값: " + value);
        }
    }

    private long parseId(String value) {
        try {
            long id = Long.parseLong(value);
            if (id <= 0) throw new NumberFormatException();
            return id;
        } catch (NumberFormatException exception) {
            throw new BusinessException(ErrorCode.RECEPTION_INVALID_REQUEST, "ID가 올바르지 않습니다.");
        }
    }

    record MyAssignedRequestResponse(
            String serviceRequestId,
            String customerName,
            String address,
            String modelName,
            LocalTime startTime,
            LocalTime endTime) {
        static MyAssignedRequestResponse from(MyAssignedRequest r) {
            return new MyAssignedRequestResponse(
                    Long.toString(r.serviceRequestId()),
                    r.customerName(),
                    r.address(),
                    r.modelName(),
                    r.startTime(),
                    r.endTime());
        }
    }

    record MyAssignedRequestDetailResponse(
            String serviceRequestId,
            String customerName,
            String customerPhone,
            String address,
            String detailAddress,
            String modelName,
            String symptom,
            String remarks,
            LocalDate visitDate,
            LocalTime startTime,
            LocalTime endTime) {
        static MyAssignedRequestDetailResponse from(MyAssignedRequestDetail d) {
            return new MyAssignedRequestDetailResponse(
                    Long.toString(d.serviceRequestId()),
                    d.customerName(),
                    d.customerPhone(),
                    d.address(),
                    d.detailAddress(),
                    d.modelName(),
                    d.symptom(),
                    d.remarks(),
                    d.visitDate(),
                    d.startTime(),
                    d.endTime());
        }
    }

    record UpdateAvailabilityRequest(String status) {}

    record MyAvailabilitySlotResponse(
            String slotId,
            LocalTime startTime,
            LocalTime endTime,
            String status,
            boolean isAssigned) {
        static MyAvailabilitySlotResponse from(MyAvailabilitySlot s) {
            return new MyAvailabilitySlotResponse(
                    Long.toString(s.slotId()),
                    s.startTime(),
                    s.endTime(),
                    s.status().name(),
                    s.isAssigned());
        }
    }
}