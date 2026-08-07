package com.noomit.backend.customer.domain;

import java.time.OffsetDateTime;
import java.util.Objects;

public record Customer(long id, String name, String phoneNumber, String zipCode, String address,
                        String detailAddress, String memo, Status status, OffsetDateTime createdAt) {
    public Customer {
        if (id <= 0) throw new IllegalArgumentException("고객 ID는 양수여야 합니다.");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("이름이 필요합니다.");
        if (phoneNumber == null || phoneNumber.isBlank()) throw new IllegalArgumentException("전화번호가 필요합니다.");
        Objects.requireNonNull(status, "고객 상태가 필요합니다.");
        Objects.requireNonNull(createdAt, "등록일시가 필요합니다.");
    }

    public enum Status {
        ACTIVE, INACTIVE;

        public static Status from(String value) {
            return Status.valueOf(value.toUpperCase());
        }
    }
}
