package com.noomit.backend.product.infrastructure.persistence;

import com.noomit.backend.product.application.CategoryRepository;
import com.noomit.backend.product.domain.Category;
import com.noomit.backend.product.domain.SubCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
class JpaCategoryRepository implements CategoryRepository {
    private final SpringDataCategoryRepository springDataCategoryRepository;
    private final SpringDataSubCategoryRepository springDataSubCategoryRepository;

    @Override
    public List<Category> findAllCategories() {
        return springDataCategoryRepository.findAll().stream()
                .map(CategoryEntity::toDomain)
                .toList();
    }

    @Override
    public List<SubCategory> findAllSubCategories() {
        return springDataSubCategoryRepository.findAll().stream()
                .map(SubCategoryEntity::toDomain)
                .toList();
    }
}
