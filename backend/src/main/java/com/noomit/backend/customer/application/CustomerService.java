package com.noomit.backend.customer.application;

import com.noomit.backend.customer.domain.Customer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomerService {
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

    @Transactional(readOnly = true)
    public Optional<Customer> search(String phoneNumber) {
        return customerRepository.findByPhoneNumber(phoneNumber);
    }

    @Transactional(readOnly = true)
    public Optional<Customer> findById(long id) {
        return customerRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public CustomerPage list(String keyword, Pageable pageable) {
        return customerRepository.search(keyword, pageable);
    }
}
