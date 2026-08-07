package com.noomit.backend.customer.application;

import com.noomit.backend.customer.domain.Customer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegisterCustomerService {
    private final CustomerRepository customerRepository;

    // 같은 phone_number로 접수되면 기존 고객을 갱신/재활성화하고, 없으면 새로 만든다.
    @Transactional
    public Customer register(RegisterCustomerCommand command) {
        return customerRepository.findByPhoneNumber(command.phoneNumber())
                .map(existing -> customerRepository.update(existing.id(), command.name(), command.zipCode(),
                        command.address(), command.detailAddress(), command.memo()))
                .orElseGet(() -> customerRepository.insert(command.name(), command.phoneNumber(), command.zipCode(),
                        command.address(), command.detailAddress(), command.memo()));
    }
}
