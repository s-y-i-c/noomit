package com.noomit.backend.reception.presentation;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import com.noomit.backend.reception.application.MyAssignedRequest;
import com.noomit.backend.reception.application.ServiceRequestQueryService;
import com.noomit.backend.shared.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/engineer/me")
@RequiredArgsConstructor
class TechnicianController {
    private final ServiceRequestQueryService serviceRequestQueryService;

    @GetMapping("/requests")
    ApiResponse<List<MyAssignedRequestResponse>> getMyRequests(
            @AuthenticationPrincipal(expression = "userId") long technicianId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<MyAssignedRequest> requests = serviceRequestQueryService.getMyAssignedRequests(technicianId, date);
        return ApiResponse.success(requests.stream()
                .map(MyAssignedRequestResponse::from)
                .toList());
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
}