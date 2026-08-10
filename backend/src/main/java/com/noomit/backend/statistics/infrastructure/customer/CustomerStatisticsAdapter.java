package com.noomit.backend.statistics.infrastructure.customer;

import java.util.Collection;
import java.util.List;
import com.noomit.backend.customer.CustomerDirectory;
import com.noomit.backend.statistics.application.port.CustomerStatisticsReader;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/** 기존 고객 공개 조회 계약을 통계 출력 포트로 연결합니다. */
@Component
@RequiredArgsConstructor
class CustomerStatisticsAdapter implements CustomerStatisticsReader {
    private final ObjectProvider<CustomerDirectory> directoryProvider;

    @Override
    public List<CustomerSnapshot> readCustomers(Collection<Long> customerIds) {
        CustomerDirectory directory = directoryProvider.getIfUnique();
        if (directory == null || customerIds.isEmpty()) return List.of();
        return directory.findByIds(customerIds).values().stream()
                .map(item -> new CustomerSnapshot(item.id(), item.name()))
                .toList();
    }

    @Override
    public boolean connected() {
        return directoryProvider.getIfUnique() != null;
    }
}
