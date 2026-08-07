package com.noomit.backend.customer.infrastructure.persistence;

import java.util.Optional;
import com.noomit.backend.customer.application.CustomerRepository;
import com.noomit.backend.customer.domain.Customer;
import com.noomit.backend.shared.error.BusinessException;
import com.noomit.backend.shared.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
class JpaCustomerRepository implements CustomerRepository {
    private final SpringDataCustomerRepository customers;

    @Override
    public Optional<Customer> findByPhoneNumber(String phoneNumber) {
        return customers.findByPhoneNumber(phoneNumber).map(CustomerEntity::toDomain);
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
            // findByPhoneNumber 조회와 save() 사이에 동시 요청이 끼어든 경우의 안전망.
            throw new BusinessException(ErrorCode.CUSTOMER_PHONE_ALREADY_EXISTS, "이미 등록된 전화번호입니다.");
        }
    }

    @Override
    public Customer update(long id, String name, String zipCode, String address, String detailAddress, String memo) {
        CustomerEntity entity = customers.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND, "고객을 찾을 수 없습니다."));
        entity.reactivateWith(name, zipCode, address, detailAddress, memo);
        return entity.toDomain();
    }
}
