package com.noomit.backend.reception.presentation;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import com.noomit.backend.reception.application.AssignServiceRequestCommand;
import com.noomit.backend.reception.application.AssignmentResult;
import com.noomit.backend.reception.application.ReassignServiceRequestCommand;
import com.noomit.backend.reception.application.ServiceRequestService;
import com.noomit.backend.shared.ApiResponse;
import com.noomit.backend.shared.error.BusinessException;
import com.noomit.backend.shared.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reception/service-requests")
@RequiredArgsConstructor
class ServiceRequestController {
    private final ServiceRequestService serviceRequestService;
    private final Clock clock;

    @PostMapping("/{id}/assign")
    ApiResponse<AssignResponse> assign(@PathVariable String id, @RequestBody AssignRequest request) {
        AssignmentResult result = serviceRequestService.assignInitial(new AssignServiceRequestCommand(
                parseId(id), parseId(request.technicianId()), parseId(request.slotId()), Instant.now(clock)));
        return ApiResponse.success("배정했습니다.", AssignResponse.from(result));
    }

    @PostMapping("/{id}/reassign")
    ApiResponse<AssignResponse> reassign(@PathVariable String id, @RequestBody ReassignRequest request) {
        if (request.version() == null) {
            throw new BusinessException(ErrorCode.RECEPTION_INVALID_REQUEST, "version이 필요합니다.");
        }
        AssignmentResult result = serviceRequestService.reassign(new ReassignServiceRequestCommand(
                parseId(id), parseId(request.technicianId()), parseId(request.slotId()),
                Instant.now(clock), request.version()));
        return ApiResponse.success("재배정했습니다.", AssignResponse.from(result));
    }

    private long parseId(String value) {
        long id;
        try {
            id = Long.parseLong(value);
        } catch (NumberFormatException exception) {
            throw new BusinessException(ErrorCode.RECEPTION_INVALID_REQUEST, "ID가 올바르지 않습니다.");
        }
        if (id <= 0) {
            throw new BusinessException(ErrorCode.RECEPTION_INVALID_REQUEST, "ID가 올바르지 않습니다.");
        }
        return id;
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
}