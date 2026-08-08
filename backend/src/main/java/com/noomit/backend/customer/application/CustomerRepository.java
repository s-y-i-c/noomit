package com.noomit.backend.customer.application;

import java.util.Optional;
import com.noomit.backend.customer.domain.Customer;
import org.springframework.data.domain.Pageable;

public interface CustomerRepository {
    Optional<Customer> findById(long id);

    Optional<Customer> findByPhoneNumber(String phoneNumber);

    /** 이름·전화번호 부분 검색으로 고객을 페이지 조회한다. status가 null이면 전체(비활성 포함). */
    CustomerPage search(String keyword, Customer.Status status, Pageable pageable);

    Customer insert(String name, String phoneNumber, String zipCode, String address,
            String detailAddress, String memo);

    Customer update(long id, String name, String zipCode, String address, String detailAddress, String memo);

    Customer changeStatus(long id, Customer.Status status);
}
