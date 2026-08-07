package com.noomit.backend.customer.presentation;

import com.noomit.backend.customer.application.CustomerService;
import com.noomit.backend.customer.domain.Customer;
import com.noomit.backend.shared.ApiResponse;
import com.noomit.backend.shared.error.BusinessException;
import com.noomit.backend.shared.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 관리자 전용 고객 관리 API.
 *
 * <p>경로가 {@code /api/v1/admin/**} 라 SecurityConfig에서 ROLE_ADMIN/ROLE_DEVELOPER만 접근 가능하다.</p>
 */
@RestController
@RequestMapping("/api/v1/admin/customers")
@RequiredArgsConstructor
class AdminCustomerController {
    private final CustomerService customerService;

    @PutMapping("/{customerId}/status")
    ApiResponse<Void> changeStatus(@PathVariable String customerId, @RequestBody String status) {
        customerService.changeStatus(parseId(customerId), parseStatus(status));
        return ApiResponse.success("상태가 변경되었습니다.", null);
    }

    private Customer.Status parseStatus(String value) {
        try {
            return Customer.Status.from(value);
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "상태 값이 올바르지 않습니다: " + value);
        }
    }

    private long parseId(String value) {
        try {
            long id = Long.parseLong(value);
            if (id <= 0) throw new NumberFormatException();
            return id;
        } catch (NumberFormatException exception) {
            throw new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND, "고객을 찾을 수 없습니다.");
        }
    }
}
