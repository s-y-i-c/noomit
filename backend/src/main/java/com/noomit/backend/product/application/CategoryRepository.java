package com.noomit.backend.product.application;

import com.noomit.backend.product.domain.Category;

import java.util.List;

public interface CategoryRepository {
    List<Category> findAllCategories();
}
