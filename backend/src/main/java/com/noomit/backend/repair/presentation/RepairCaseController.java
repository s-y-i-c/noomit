package com.noomit.backend.repair.presentation;

import com.noomit.backend.repair.application.RepairCaseService;
import com.noomit.backend.repair.domain.RepairCase;
import com.noomit.backend.shared.ApiResponse;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/repair-cases")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ENGINEER')")
class RepairCaseController {

    private final RepairCaseService repairCaseService;

    @GetMapping("/my")
    ApiResponse<List<RepairCaseResponse>> getMyRepairCases(@AuthenticationPrincipal(expression = "userId") long userId) {
        List<RepairCase> cases = repairCaseService.getMyRepairCases(userId);
        return ApiResponse.success(cases.stream().map(RepairCaseResponse::from).toList());
    }

    @GetMapping("/{id}")
    ApiResponse<RepairCaseResponse> getRepairCase(
            @PathVariable Long id,
            @AuthenticationPrincipal(expression = "userId") long userId) {
        RepairCase repairCase = repairCaseService.getRepairCase(id, userId);
        return ApiResponse.success(RepairCaseResponse.from(repairCase));
    }

    @PostMapping("/{id}/details")
    ApiResponse<RepairCaseResponse> addDetail(
            @PathVariable Long id,
            @RequestBody @Valid AddRepairDetailRequest request,
            @AuthenticationPrincipal(expression = "userId") long userId) {
        RepairCase repairCase = repairCaseService.addDetail(id, userId, request.description(), request.amount());
        return ApiResponse.success("수리내역을 추가했습니다.", RepairCaseResponse.from(repairCase));
    }

    @DeleteMapping("/{id}/details/{detailId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteDetail(
            @PathVariable Long id,
            @PathVariable Long detailId,
            @AuthenticationPrincipal(expression = "userId") long userId) {
        repairCaseService.deleteDetail(id, detailId, userId);
    }

    @PutMapping("/{id}/submit")
    ApiResponse<RepairCaseResponse> submit(
            @PathVariable Long id,
            @AuthenticationPrincipal(expression = "userId") long userId) {
        RepairCase repairCase = repairCaseService.submit(id, userId);
        return ApiResponse.success("완료 제출했습니다.", RepairCaseResponse.from(repairCase));
    }

    record AddRepairDetailRequest(
            @NotBlank String description,
            @Positive BigDecimal amount
    ) {}
}
