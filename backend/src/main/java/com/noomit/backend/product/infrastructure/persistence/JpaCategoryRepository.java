package com.noomit.backend.product.infrastructure.persistence;

import com.noomit.backend.product.application.CategoryRepository;
import com.noomit.backend.product.domain.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
class JpaCategoryRepository implements CategoryRepository {
    private final SpringDataCategoryRepository springDataCategoryRepository;
    @Override
    public List<Category> findAllCategories() {
        return springDataCategoryRepository.findAll().stream()
                .map(CategoryEntity::toDomain)
                .toList();
    }
}
