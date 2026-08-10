package com.noomit.backend.product.application;

public record RegisterProductCommand(long subCategoryId, String modelName, String modelCode, String memo) {
    public RegisterProductCommand {
        if (subCategoryId <= 0) {
            throw new IllegalArgumentException("서브카테고리 ID가 필요합니다.");
        }
        if (modelName == null || modelName.isBlank()) {
            throw new IllegalArgumentException("모델명이 필요합니다.");
        }
        if (modelCode == null || modelCode.isBlank()) {
            throw new IllegalArgumentException("모델코드가 필요합니다.");
        }
    }
}
