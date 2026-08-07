package com.noomit.backend.customer.application;

public record RegisterCustomerCommand(String name, String phoneNumber, String zipCode,
        String address, String detailAddress, String memo) {
    public RegisterCustomerCommand {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("이름이 필요합니다.");
        }
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalArgumentException("전화번호가 필요합니다.");
        }
        if (zipCode == null || zipCode.isBlank()) {
            throw new IllegalArgumentException("우편번호가 필요합니다.");
        }
        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException("주소가 필요합니다.");
        }
    }
}
