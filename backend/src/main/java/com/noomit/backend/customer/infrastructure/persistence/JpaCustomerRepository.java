package com.noomit.backend.customer.infrastructure.persistence;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import com.noomit.backend.customer.application.CustomerPage;
import com.noomit.backend.customer.application.CustomerRepository;
import com.noomit.backend.customer.domain.Customer;
import com.noomit.backend.shared.error.BusinessException;
import com.noomit.backend.shared.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
class JpaCustomerRepository implements CustomerRepository {
    private final SpringDataCustomerRepository customers;

    @Override
    public Optional<Customer> findById(long id) {
        return customers.findById(id).map(CustomerEntity::toDomain);
    }

    @Override
    public Optional<Customer> findByPhoneNumber(String phoneNumber) {
        return customers.findByPhoneNumber(phoneNumber).map(CustomerEntity::toDomain);
    }

    @Override
    public CustomerPage search(String keyword, Pageable pageable) {
        String verifiedKeyword = keyword == null ? "" : keyword.trim().toLowerCase();
        Page<CustomerEntity> result = customers.search(verifiedKeyword, pageable);
        List<Customer> found = result.stream().map(CustomerEntity::toDomain).toList();
        return new CustomerPage(found, pageable.getPageNumber(), pageable.getPageSize(),
                result.getTotalElements(), result.getTotalPages());
    }

    @Override
    public Customer insert(String name, String phoneNumber, String zipCode, String address,
            String detailAddress, String memo) {
        CustomerEntity entity = CustomerEntity.builder()
                .name(name)
                .phoneNumber(phoneNumber)
                .zipCode(zipCode)
                .address(address)
                .detailAddress(detailAddress)
                .memo(memo)
                .build();
        try {
            return customers.save(entity).toDomain();
        } catch (DataIntegrityViolationException exception) {
            // findByPhoneNumber 조회와 save() 사이에 동시 요청이 끼어들어 다른 요청이 먼저 이 번호로
            // 생성해버린 경우다. upsert 의미상 이 요청도 실패가 아니라 "업데이트"로 이어져야 한다.
            CustomerEntity winner = customers.findByPhoneNumber(phoneNumber)
                    .orElseThrow(() -> new BusinessException(
                            ErrorCode.CUSTOMER_PHONE_ALREADY_EXISTS, "이미 등록된 전화번호입니다."));
            return reactivate(winner, name, zipCode, address, detailAddress, memo);
        }
    }

    @Override
    public Customer update(long id, String name, String zipCode, String address, String detailAddress, String memo) {
        CustomerEntity entity = customers.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND, "고객을 찾을 수 없습니다."));
        return reactivate(entity, name, zipCode, address, detailAddress, memo);
    }

    @Override
    public Customer changeStatus(long id, Customer.Status status) {
        CustomerEntity entity = customers.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND, "고객을 찾을 수 없습니다."));
        entity.changeStatus(status);
        return entity.toDomain();
    }

    private Customer reactivate(CustomerEntity entity, String name, String zipCode, String address,
            String detailAddress, String memo) {
        entity.reactivateWith(name, zipCode, address, detailAddress, memo);
        return entity.toDomain();
    }
}
