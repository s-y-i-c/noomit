package com.noomit.backend.statistics.application.port;

import java.util.Collection;
import java.util.List;

/** 고객 이름은 N+1 조회를 피하도록 ID 집합으로 한 번에 요청한다. */
public interface CustomerStatisticsReader {
    List<CustomerSnapshot> readCustomers(Collection<String> customerIds);

    default boolean connected() {
        return true;
    }

    record CustomerSnapshot(String customerId, String customerName) {}
}
