package com.noomit.backend.reception.presentation;

import java.time.LocalDate;
import java.time.LocalTime;
import com.noomit.backend.reception.application.ServiceRequestDetail;
import com.noomit.backend.reception.application.ServiceRequestQueryService;
import com.noomit.backend.shared.ApiResponse;
import com.noomit.backend.shared.error.BusinessException;
import com.noomit.backend.shared.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/engineer/service-requests")
@PreAuthorize("hasAnyRole('ENGINEER', 'ADMIN')")
@RequiredArgsConstructor
class ServiceRequestEngineerController {

    private final ServiceRequestQueryService serviceRequestQueryService;

    @GetMapping("/{id}")
    ApiResponse<ServiceRequestSummaryResponse> getSummary(@PathVariable String id) {
        ServiceRequestDetail detail = serviceRequestQueryService.getDetail(parseId(id));
        return ApiResponse.success(ServiceRequestSummaryResponse.from(detail));
    }

    private long parseId(String value) {
        try {
            long parsed = Long.parseLong(value);
            if (parsed <= 0) throw new NumberFormatException();
            return parsed;
        } catch (NumberFormatException e) {
            throw new BusinessException(ErrorCode.RECEPTION_INVALID_REQUEST, "ID가 올바르지 않습니다.");
        }
    }

    record ServiceRequestSummaryResponse(
            String id,
            String requestNumber,
            String symptom,
            String modelName,
            String subCategoryName,
            String customerName,
            LocalDate visitDate,
            LocalTime visitStartTime,
            LocalTime visitEndTime,
            String address,
            String detailAddress,
            String remarks
    ) {
        static ServiceRequestSummaryResponse from(ServiceRequestDetail detail) {
            return new ServiceRequestSummaryResponse(
                    Long.toString(detail.id()),
                    detail.requestNumber(),
                    detail.symptom(),
                    detail.modelName(),
                    detail.subCategoryName(),
                    detail.customerName(),
                    detail.visitDate(),
                    detail.visitStartTime(),
                    detail.visitEndTime(),
                    detail.address(),
                    detail.detailAddress(),
                    detail.remarks()
            );
        }
    }
}
