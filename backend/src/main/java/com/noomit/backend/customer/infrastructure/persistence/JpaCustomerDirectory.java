package com.noomit.backend.customer.infrastructure.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import com.noomit.backend.customer.CustomerDirectory;
import com.noomit.backend.customer.CustomerInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
class JpaCustomerDirectory implements CustomerDirectory {
    private final SpringDataCustomerRepository customers;

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

    private static CustomerInfo toInfo(CustomerEntity entity) {
        return new CustomerInfo(entity.getId(), entity.getName(), entity.getPhoneNumber(),
                entity.getAddress(), entity.getDetailAddress());
    }
}
