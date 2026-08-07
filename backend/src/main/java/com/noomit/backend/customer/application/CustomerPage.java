package com.noomit.backend.customer.application;

import java.util.List;
import com.noomit.backend.customer.domain.Customer;

/** 고객 목록 한 페이지. */
public record CustomerPage(List<Customer> customers, int page, int size, long totalElements, int totalPages) {
}
