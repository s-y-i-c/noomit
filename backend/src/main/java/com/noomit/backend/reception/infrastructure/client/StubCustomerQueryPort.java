package com.noomit.backend.reception.infrastructure.client;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.noomit.backend.reception.application.customer.CustomerInfo;
import com.noomit.backend.reception.application.customer.CustomerQueryPort;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

// TODO: customer 모듈에 실제 CustomerQueryPort 구현체가 생기면 이 클래스를 삭제한다.
@Component
@Profile("!prod")
public class StubCustomerQueryPort implements CustomerQueryPort {

    @Override
    public Optional<CustomerInfo> getCustomer(Long customerId) {
        return Optional.of(new CustomerInfo(customerId, "홍길동", "010-0000-0000", "서울시 강남구", "101동 101호"));
    }

    @Override
    public Map<Long, CustomerInfo> getCustomers(List<Long> customerIds) {
        return customerIds.stream()
                .collect(Collectors.toMap(id -> id,
                        id -> new CustomerInfo(id, "고객" + id, "010-0000-0000", "주소", "상세주소")));
    }
}