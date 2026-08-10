package com.noomit.backend.product.domain;

import java.time.OffsetDateTime;
import java.util.Objects;

public record Product(long id, long categoryId, long subCategoryId, String modelName, String modelCode,
                       String memo, Status status, OffsetDateTime createdAt) {
    public Product {
        if (id <= 0) throw new IllegalArgumentException("제품 ID는 양수여야 합니다.");
        if (categoryId <= 0) throw new IllegalArgumentException("카테고리 ID가 필요합니다.");
        if (subCategoryId <= 0) throw new IllegalArgumentException("서브카테고리 ID가 필요합니다.");
        if (modelName == null || modelName.isBlank()) throw new IllegalArgumentException("모델명이 필요합니다.");
        if (modelCode == null || modelCode.isBlank()) throw new IllegalArgumentException("모델코드가 필요합니다.");
        Objects.requireNonNull(status, "제품 상태가 필요합니다.");
        Objects.requireNonNull(createdAt, "등록일시가 필요합니다.");
    }

    public enum Status {
        ACTIVE, INACTIVE;

        public static Status from(String value) {
            return Status.valueOf(value.toUpperCase());
        }
    }
}
