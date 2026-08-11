package com.noomit.backend.customer;

/**
 * 다른 모듈이 고객을 새로 만들거나 갱신할 때 쓰는 요청.
 * 전화번호로 upsert된다 — 있으면 갱신/재활성화, 없으면 새로 만든다.
 */
public record UpsertCustomerCommand(String name, String phoneNumber, String zipCode,
        String address, String detailAddress, String memo) {
}
