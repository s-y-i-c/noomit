package com.noomit.backend.customer.presentation;

import com.noomit.backend.customer.application.RegisterCustomerCommand;
import com.noomit.backend.customer.application.RegisterCustomerService;
import com.noomit.backend.customer.domain.Customer;
import com.noomit.backend.shared.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
class CustomerController {
    private final RegisterCustomerService registerCustomerService;

    // 같은 전화번호로 이미 접수된 고객이면 갱신, 아니면 새로 등록한다 (upsert).
    @PostMapping
    ApiResponse<CustomerResponse> registerCustomer(@RequestBody RegisterCustomerRequest request) {
        Customer result = registerCustomerService.register(new RegisterCustomerCommand(
                request.name(), request.phoneNumber(), request.zipCode(),
                request.address(), request.detailAddress(), request.memo()));
        return ApiResponse.success("고객 정보를 반영했습니다.", CustomerResponse.from(result));
    }

    record RegisterCustomerRequest(String name, String phoneNumber, String zipCode,
                                   String address, String detailAddress, String memo) {}

    record CustomerResponse(String id, String name, String phoneNumber, String zipCode,
                            String address, String detailAddress, String memo, String status) {
        static CustomerResponse from(Customer customer) {
            return new CustomerResponse(Long.toString(customer.id()), customer.name(), customer.phoneNumber(),
                    customer.zipCode(), customer.address(), customer.detailAddress(), customer.memo(),
                    customer.status().name());
        }
    }
}
