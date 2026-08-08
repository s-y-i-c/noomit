package com.noomit.backend.reception.application.customer;

import java.util.List;
import java.util.Map;

/** reception이 소비하는 고객 조회 창구. 임시 */
public interface CustomerQueryPort {
    CustomerInfo getCustomer(Long customerId);

    Map<Long, CustomerInfo> getCustomers(List<Long> customerIds);
}