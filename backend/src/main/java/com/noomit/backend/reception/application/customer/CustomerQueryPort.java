package com.noomit.backend.reception.application.customer;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/** reception이 소비하는 고객 조회 창구. 임시 */
public interface CustomerQueryPort {
    Optional<CustomerInfo> getCustomer(Long customerId);

    Map<Long, CustomerInfo> getCustomers(List<Long> customerIds);
}