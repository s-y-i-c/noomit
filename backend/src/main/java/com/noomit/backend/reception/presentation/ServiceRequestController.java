package com.noomit.backend.reception.presentation;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import com.noomit.backend.reception.application.AssignServiceRequestCommand;
import com.noomit.backend.reception.application.AssignmentResult;
import com.noomit.backend.reception.application.CancelServiceRequestCommand;
import com.noomit.backend.reception.application.CancellationResult;
import com.noomit.backend.reception.application.CreateServiceRequestCommand;
import com.noomit.backend.reception.application.ReassignServiceRequestCommand;
import com.noomit.backend.reception.application.ServiceRequestService;
import com.noomit.backend.reception.application.UpdateServiceRequestCommand;
import com.noomit.backend.reception.domain.ServiceRequest;
import com.noomit.backend.shared.ApiResponse;
import com.noomit.backend.shared.error.BusinessException;
import com.noomit.backend.shared.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/counselor/reception/requests")
@RequiredArgsConstructor
class ServiceRequestController {
    private final ServiceRequestService serviceRequestService;

    @GetMapping("/base-fee")
    ApiResponse<BaseFeeResponse> getBaseFee() {
        return ApiResponse.success(new BaseFeeResponse(serviceRequestService.getBaseFeePolicy()));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ApiResponse<ServiceRequestResponse> create(
            @AuthenticationPrincipal(expression = "userId") long receptionistId,
            @RequestBody CreateRequest request) {
        ServiceRequest created = serviceRequestService.create(new CreateServiceRequestCommand(
                request.customerName(), request.customerPhoneNumber(), request.customerZipCode(),
                request.customerAddress(), request.customerDetailAddress(), request.customerMemo(),
                parseOptionalId(request.productId()), parseOptionalId(request.selectedSubCategoryId()),
                request.selectedModelName(), receptionistId, request.symptom(), request.remarks()));
        return ApiResponse.success("접수를 생성했습니다.", ServiceRequestResponse.from(created));
    }

    @PatchMapping("/{id}")
    ApiResponse<ServiceRequestResponse> update(@PathVariable String id, @RequestBody UpdateRequest request) {
        if (request.version() == null) {
            throw new BusinessException(ErrorCode.RECEPTION_INVALID_REQUEST, "version이 필요합니다.");
        }
        ServiceRequest updated = serviceRequestService.update(new UpdateServiceRequestCommand(
                parseId(id), request.customerName(), request.customerPhoneNumber(), request.customerZipCode(),
                request.customerAddress(), request.customerDetailAddress(), request.customerMemo(),
                parseOptionalId(request.productId()), parseOptionalId(request.selectedSubCategoryId()),
                request.selectedModelName(), request.symptom(), request.remarks(), request.version()));
        return ApiResponse.success("접수 정보를 수정했습니다.", ServiceRequestResponse.from(updated));
    }

    @PostMapping("/{id}/assign")
    ApiResponse<AssignResponse> assign(@PathVariable String id, @RequestBody AssignRequest request) {
        AssignmentResult result = serviceRequestService.assignInitial(new AssignServiceRequestCommand(
                parseId(id), parseId(request.technicianId()), parseId(request.slotId())));
        return ApiResponse.success("배정했습니다.", AssignResponse.from(result));
    }

    @PostMapping("/{id}/reassign")
    ApiResponse<AssignResponse> reassign(@PathVariable String id, @RequestBody ReassignRequest request) {
        if (request.version() == null) {
            throw new BusinessException(ErrorCode.RECEPTION_INVALID_REQUEST, "version이 필요합니다.");
        }
        AssignmentResult result = serviceRequestService.reassign(new ReassignServiceRequestCommand(
                parseId(id), parseId(request.technicianId()), parseId(request.slotId()), request.version()));
        return ApiResponse.success("재배정했습니다.", AssignResponse.from(result));
    }

    @PatchMapping("/{id}/cancel")
    ApiResponse<CancelResponse> cancel(@PathVariable String id, @RequestBody CancelRequest request) {
        CancellationResult result = serviceRequestService.cancel(new CancelServiceRequestCommand(
                parseId(id), request.cancelReason()));
        return ApiResponse.success("접수를 취소했습니다.", CancelResponse.from(result));
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

    private Long parseOptionalId(String value) {
        return (value == null || value.isBlank()) ? null : parseId(value);
    }

    record BaseFeeResponse(int baseFee) {}

    record CreateRequest(
            String customerName,
            String customerPhoneNumber,
            String customerZipCode,
            String customerAddress,
            String customerDetailAddress,
            String customerMemo,
            String productId,
            String selectedSubCategoryId,
            String selectedModelName,
            String symptom,
            String remarks) {}

    record UpdateRequest(
            String customerName,
            String customerPhoneNumber,
            String customerZipCode,
            String customerAddress,
            String customerDetailAddress,
            String customerMemo,
            String productId,
            String selectedSubCategoryId,
            String selectedModelName,
            String symptom,
            String remarks,
            Long version) {}

    record ServiceRequestResponse(
            String id,
            String requestNumber,
            String status,
            String symptom,
            String remarks,
            int baseFee,
            Instant requestedAt) {
        static ServiceRequestResponse from(ServiceRequest r) {
            return new ServiceRequestResponse(
                    Long.toString(r.id()),
                    r.requestNumber(),
                    r.status().name(),
                    r.symptom(),
                    r.remarks(),
                    r.baseFee(),
                    r.requestedAt());
        }
    }

    record AssignRequest(String technicianId, String slotId) {}

    record ReassignRequest(String technicianId, String slotId, Long version) {}

    record AssignResponse(
            String id,
            String status,
            String technicianName,
            LocalDate visitDate,
            LocalTime visitStartTime,
            LocalTime visitEndTime) {
        static AssignResponse from(AssignmentResult result) {
            return new AssignResponse(
                    Long.toString(result.id()),
                    result.status().name(),
                    result.technicianName(),
                    result.visitDate(),
                    result.visitStartTime(),
                    result.visitEndTime());
        }
    }

    record CancelRequest(String cancelReason) {}

    record CancelResponse(String id, String status, String cancelReason, Instant cancelledAt) {
        static CancelResponse from(CancellationResult result) {
            return new CancelResponse(
                    Long.toString(result.id()),
                    result.status().name(),
                    result.cancelReason(),
                    result.cancelledAt());
        }
    }
}