package com.noomit.backend.repair.presentation;

import com.noomit.backend.repair.application.RepairCaseService;
import com.noomit.backend.repair.domain.RepairCase;
import com.noomit.backend.repair.domain.RepairStatus;
import com.noomit.backend.shared.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/repair-cases")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
class RepairCaseAdminController {

    private final RepairCaseService repairCaseService;

    @GetMapping
    ApiResponse<List<RepairCaseResponse>> getAllRepairCases(
            @RequestParam(required = false) RepairStatus status) {
        List<RepairCase> cases = repairCaseService.getAllRepairCases(status);
        return ApiResponse.success(cases.stream().map(RepairCaseResponse::from).toList());
    }

    @PutMapping("/{id}/approve")
    ApiResponse<RepairCaseResponse> approve(@PathVariable Long id) {
        RepairCase repairCase = repairCaseService.approve(id);
        return ApiResponse.success("완료 승인했습니다.", RepairCaseResponse.from(repairCase));
    }

    @PutMapping("/{id}/reject")
    ApiResponse<RepairCaseResponse> reject(
            @PathVariable Long id,
            @RequestBody @Valid RejectRequest request) {
        RepairCase repairCase = repairCaseService.reject(id, request.reason());
        return ApiResponse.success("반려했습니다.", RepairCaseResponse.from(repairCase));
    }

    record RejectRequest(@NotBlank String reason) {}
}
