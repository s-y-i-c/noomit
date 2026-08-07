package com.noomit.backend.customer.presentation;

import com.noomit.backend.customer.application.CustomerPage;
import com.noomit.backend.customer.application.CustomerService;
import com.noomit.backend.customer.application.RegisterCustomerCommand;
import com.noomit.backend.customer.domain.Customer;
import com.noomit.backend.shared.ApiResponse;
import com.noomit.backend.shared.error.BusinessException;
import com.noomit.backend.shared.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
class CustomerController {
    private final CustomerService customerService;

    // 같은 전화번호로 이미 접수된 고객이면 갱신, 아니면 새로 등록한다 (upsert).
    @PostMapping
    ApiResponse<CustomerResponse> registerCustomer(@RequestBody RegisterCustomerRequest request) {
        Customer result = customerService.register(new RegisterCustomerCommand(
                request.name(), request.phoneNumber(), request.zipCode(),
                request.address(), request.detailAddress(), request.memo()));
        return ApiResponse.success("고객 정보를 반영했습니다.", CustomerResponse.from(result));
    }

    @GetMapping("/phone")
    ApiResponse<CustomerResponse> searchCustomer(@RequestParam String phoneNumber) {
        Optional<Customer> result = customerService.search(phoneNumber);
        return result
                .map(customer -> ApiResponse.success("전화번호로 조회한 고객 상세 정보", CustomerResponse.from(customer)))
                .orElseGet(() -> ApiResponse.success("신규 유저입니다.", null));
    }

    @GetMapping("/{id}")
    ApiResponse<CustomerResponse> getCustomer(@PathVariable String id) {
        Customer customer = customerService.findById(parseId(id))
                .orElseThrow(() -> new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND, "고객을 찾을 수 없습니다."));
        return ApiResponse.success(CustomerResponse.from(customer));
    }

    // ?page=0&size=20&sort=name&keyword=홍길동(또는 전화번호)&status=ACTIVE(또는 INACTIVE, 생략 시 전체)
    @GetMapping
    ApiResponse<CustomerPageResponse> listCustomers(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) String status,
            @PageableDefault(sort = "name") Pageable pageable) {
        Customer.Status parsedStatus = (status == null || status.isBlank()) ? null : Customer.Status.from(status);
        CustomerPage result = customerService.list(keyword, parsedStatus, pageable);
        return ApiResponse.success(CustomerPageResponse.from(result));
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

    record RegisterCustomerRequest(String name, String phoneNumber, String zipCode,
                                   String address, String detailAddress, String memo) {
    }

    record CustomerResponse(String id, String name, String phoneNumber, String zipCode,
                            String address, String detailAddress, String memo, String status) {
        static CustomerResponse from(Customer customer) {
            return new CustomerResponse(Long.toString(customer.id()), customer.name(), customer.phoneNumber(),
                    customer.zipCode(), customer.address(), customer.detailAddress(), customer.memo(),
                    customer.status().name());
        }
    }

    record CustomerPageResponse(List<CustomerResponse> customers, int page, int size,
            long totalElements, int totalPages) {
        static CustomerPageResponse from(CustomerPage page) {
            List<CustomerResponse> customers = page.customers().stream()
                    .map(CustomerResponse::from).toList();
            return new CustomerPageResponse(customers, page.page(), page.size(),
                    page.totalElements(), page.totalPages());
        }
    }
}
