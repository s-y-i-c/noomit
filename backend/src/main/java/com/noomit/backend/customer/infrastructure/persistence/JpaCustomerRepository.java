package com.noomit.backend.customer.infrastructure.persistence;

import java.util.List;
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
        return customers.findByPhoneNumber(normalizePhone(phoneNumber)).map(CustomerEntity::toDomain);
    }

    @Override
    public CustomerPage search(String keyword, Customer.Status status, Pageable pageable) {
        // 검색어에 낀 공백·하이픈도 이름/전화번호 쪽과 똑같이 지워서 비교한다
        // (SpringDataCustomerRepository.search 참고).
        String verifiedKeyword = keyword == null ? "" : keyword.toLowerCase().replace(" ", "").replace("-", "");
        Page<CustomerEntity> result = customers.search(verifiedKeyword, status, pageable);
        List<Customer> found = result.stream().map(CustomerEntity::toDomain).toList();
        return new CustomerPage(found, pageable.getPageNumber(), pageable.getPageSize(),
                result.getTotalElements(), result.getTotalPages());
    }

    @Override
    public Customer insert(String name, String phoneNumber, String zipCode, String address,
            String detailAddress, String memo) {
        // 저장 시점에 하이픈을 지워서, DB엔 항상 숫자만 있는 형태로 남긴다.
        // (조회 때마다 REPLACE()로 비교하면 UNIQUE 인덱스를 못 타고, 하이픈 유무만 다른
        // 같은 번호가 두 번 저장될 수도 있어서 저장 시점에 정규화하는 쪽으로 통일한다.)
        String normalizedPhoneNumber = normalizePhone(phoneNumber);
        CustomerEntity entity = CustomerEntity.builder()
                .name(name)
                .phoneNumber(normalizedPhoneNumber)
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
            CustomerEntity winner = customers.findByPhoneNumber(normalizedPhoneNumber)
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

    // DB엔 "010-1234-5678"처럼 하이픈 포함해 저장돼 있어도, 조회는 하이픈 유무 상관없이 되게 한다.
    private static String normalizePhone(String phoneNumber) {
        return phoneNumber == null ? "" : phoneNumber.replace("-", "");
    }
}
