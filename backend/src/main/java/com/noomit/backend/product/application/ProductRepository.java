package com.noomit.backend.product.application;

import java.util.Optional;
import com.noomit.backend.product.domain.Product;

public interface ProductRepository {
    Optional<Product> findById(long id);

    Product insert(long subCategoryId, String modelName, String modelCode, String memo);
}
