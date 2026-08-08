package com.noomit.backend.reception.presentation;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import com.noomit.backend.reception.application.PageResult;
import com.noomit.backend.reception.application.ServiceRequestListItem;
import com.noomit.backend.reception.application.ServiceRequestListQuery;
import com.noomit.backend.reception.application.ServiceRequestQueryService;
import com.noomit.backend.shared.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/counselor/reception/requests")
@RequiredArgsConstructor
class ServiceRequestQueryController {
    private final ServiceRequestQueryService serviceRequestQueryService;

    @GetMapping
    ApiResponse<ServiceRequestListResponse> list(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort) {
        PageResult<ServiceRequestListItem> result = serviceRequestQueryService.findList(
                ServiceRequestListQuery.of(status, sort, page, size));

        return ApiResponse.success(ServiceRequestListResponse.from(result));
    }

    record ServiceRequestListResponse(
            List<Item> content,
            int page,
            int size,
            long totalElements,
            int totalPages) {
        record Item(
                String id,
                String customerName,
                String customerPhone,
                String modelName,
                String symptom,
                String status,
                String technicianName,
                LocalDate visitDate,
                LocalTime visitStartTime,
                LocalTime visitEndTime,
                Instant requestedAt) {
            static Item from(ServiceRequestListItem item) {
                return new Item(
                        Long.toString(item.id()),
                        item.customerName(),
                        item.customerPhone(),
                        item.modelName(),
                        item.symptom(),
                        item.status().name(),
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
}