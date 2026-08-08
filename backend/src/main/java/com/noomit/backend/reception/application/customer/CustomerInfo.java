package com.noomit.backend.reception.application.customer;

public record CustomerInfo(
        Long customerId,
        String name,
        String phoneNumber,
        String address,
        String detailAddress) {
}