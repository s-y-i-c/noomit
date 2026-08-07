package com.noomit.backend.customer.application;

import java.util.Optional;
import com.noomit.backend.customer.domain.Customer;

public interface CustomerRepository {
    Optional<Customer> findByPhoneNumber(String phoneNumber);

    Customer insert(String name, String phoneNumber, String zipCode, String address,
            String detailAddress, String memo);

    Customer update(long id, String name, String zipCode, String address, String detailAddress, String memo);
}
