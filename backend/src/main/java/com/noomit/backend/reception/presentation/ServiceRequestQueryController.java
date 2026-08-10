package com.noomit.backend.reception.presentation;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import com.noomit.backend.reception.application.PageResult;
import com.noomit.backend.reception.application.ServiceRequestDetail;
import com.noomit.backend.reception.application.ServiceRequestListItem;
import com.noomit.backend.reception.application.ServiceRequestListQuery;
import com.noomit.backend.reception.application.ServiceRequestQueryService;
import com.noomit.backend.shared.ApiResponse;
import com.noomit.backend.shared.error.BusinessException;
import com.noomit.backend.shared.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/counselor/reception/requests")
@RequiredArgsConstructor
class ServiceRequestQueryController {
    private final ServiceRequestQueryService serviceRequestQueryService;

    @GetMapping
    ApiResponse<ServiceRequestListResponse> getList(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort) {
        PageResult<ServiceRequestListItem> result = serviceRequestQueryService.getList(
                ServiceRequestListQuery.of(status, sort, page, size));

        return ApiResponse.success(ServiceRequestListResponse.from(result));
    }

    @GetMapping("/{id}")
    ApiResponse<ServiceRequestDetailResponse> getDetail(@PathVariable String id) {
        ServiceRequestDetail detail = serviceRequestQueryService.getDetail(parseId(id));
        return ApiResponse.success(ServiceRequestDetailResponse.from(detail));
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

    record ServiceRequestListResponse(
            List<Item> content,
            int page,
            int size,
            long totalElements,
            int totalPages) {
        record Item(
                String id,
                String requestNumber,
                String customerName,
                String customerPhone,
                String modelName,
                String symptom,
                String status,
                String receptionistName,
                String technicianName,
                LocalDate visitDate,
                LocalTime visitStartTime,
                LocalTime visitEndTime,
                Instant requestedAt) {
            static Item from(ServiceRequestListItem item) {
                return new Item(
                        Long.toString(item.id()),
                        item.requestNumber(),
                        item.customerName(),
                        item.customerPhone(),
                        item.modelName(),
                        item.symptom(),
                        item.status().name(),
                        item.receptionistName(),
                        item.technicianName(),
                        item.visitDate(),
                        item.visitStartTime(),
                        item.visitEndTime(),
                        item.requestedAt());
            }
        }

        static ServiceRequestListResponse from(PageResult<ServiceRequestListItem> page) {
            int totalPages = (int) Math.ceil((double) page.totalElements() / page.size());
            return new ServiceRequestListResponse(
                    page.content().stream().map(Item::from).toList(),
                    page.page(), page.size(), page.totalElements(), totalPages);
        }
    }

    record ServiceRequestDetailResponse(
            String id,
            String requestNumber,
            String customerName,
            String customerPhone,
            String modelName,
            String symptom,
            String status,
            String receptionistName,
            String technicianName,
            LocalDate visitDate,
            LocalTime visitStartTime,
            LocalTime visitEndTime,
            String address,
            String detailAddress,
            String remarks,
            int baseFee,
            Instant requestedAt,
            Instant assignedAt,
            Instant cancelledAt,
            String cancelReason,
            long version) {
        static ServiceRequestDetailResponse from(ServiceRequestDetail detail) {
            return new ServiceRequestDetailResponse(
                    Long.toString(detail.id()),
                    detail.requestNumber(),
                    detail.customerName(),
                    detail.customerPhone(),
                    detail.modelName(),
                    detail.symptom(),
                    detail.status().name(),
                    detail.receptionistName(),
                    detail.technicianName(),
                    detail.visitDate(),
                    detail.visitStartTime(),
                    detail.visitEndTime(),
                    detail.address(),
                    detail.detailAddress(),
                    detail.remarks(),
                    detail.baseFee(),
                    detail.requestedAt(),
                    detail.assignedAt(),
                    detail.cancelledAt(),
                    detail.cancelReason(),
                    detail.version());
        }
    }
}