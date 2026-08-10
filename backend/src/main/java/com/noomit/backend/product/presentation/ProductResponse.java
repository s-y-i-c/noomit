package com.noomit.backend.product.presentation;

import com.noomit.backend.product.domain.Product;

public record ProductResponse(String id, long categoryId, long subCategoryId, String modelName,
                              String modelCode, String memo, String status) {
    static ProductResponse from(Product product) {
        return new ProductResponse(Long.toString(product.id()), product.categoryId(), product.subCategoryId(),
                product.modelName(), product.modelCode(), product.memo(), product.status().name());
    }
}
