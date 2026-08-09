package com.noomit.backend.product.application;

import com.noomit.backend.product.domain.Category;
import com.noomit.backend.product.domain.Product;
import com.noomit.backend.product.domain.SubCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public List<Category> getCategoryList() {
        return categoryRepository.findAllCategories();
    }

    public List<SubCategory> getSubCategoryList() {
        return categoryRepository.findAllSubCategories();
    }

    @Transactional
    public Product registerProduct(RegisterProductCommand command) {
        return productRepository.insert(command.subCategoryId(), command.modelName(),
                command.modelCode(), command.memo());
    }
}
