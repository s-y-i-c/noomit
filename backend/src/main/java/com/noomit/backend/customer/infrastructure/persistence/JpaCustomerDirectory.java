package com.noomit.backend.customer.infrastructure.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import com.noomit.backend.customer.CustomerDirectory;
import com.noomit.backend.customer.CustomerInfo;
import com.noomit.backend.customer.UpsertCustomerCommand;
import com.noomit.backend.customer.application.CustomerService;
import com.noomit.backend.customer.application.RegisterCustomerCommand;
import com.noomit.backend.customer.domain.Customer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
class JpaCustomerDirectory implements CustomerDirectory {
    private final SpringDataCustomerRepository customers;
    private final CustomerService customerService;

    @Override
    public Optional<CustomerInfo> findById(long customerId) {
        return customers.findById(customerId).map(JpaCustomerDirectory::toInfo);
    }

    @Override
    public Map<Long, CustomerInfo> findByIds(Collection<Long> customerIds) {
        if (customerIds == null || customerIds.isEmpty()) return Map.of();
        List<Long> distinctIds = customerIds.stream().distinct().toList();
        return customers.findAllById(distinctIds).stream()
                .map(JpaCustomerDirectory::toInfo)
                .collect(Collectors.toMap(CustomerInfo::id, Function.identity()));
    }

    @Override
    public CustomerInfo upsert(UpsertCustomerCommand command) {
        // upsert 로직 자체는 CustomerService.register()에 이미 있어서 그대로 위임한다 — 여기서 새로 만들지 않는다.
        Customer customer = customerService.register(new RegisterCustomerCommand(
                command.name(), command.phoneNumber(), command.zipCode(),
                command.address(), command.detailAddress(), command.memo()));
        return toInfo(customer);
    }

    private static CustomerInfo toInfo(CustomerEntity entity) {
        return new CustomerInfo(entity.getId(), entity.getName(), entity.getPhoneNumber(),
                entity.getAddress(), entity.getDetailAddress());
    }

    private static CustomerInfo toInfo(Customer customer) {
        return new CustomerInfo(customer.id(), customer.name(), customer.phoneNumber(),
                customer.address(), customer.detailAddress());
    }
}
