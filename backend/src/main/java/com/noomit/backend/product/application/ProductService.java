package com.noomit.backend.product.application;

import com.noomit.backend.product.domain.Category;
import com.noomit.backend.product.domain.Product;
import com.noomit.backend.product.domain.SubCategory;
import com.noomit.backend.shared.error.BusinessException;
import com.noomit.backend.shared.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
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

    @Transactional(readOnly = true)
    public Product findById(long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "제품을 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public ProductPage list(String keyword, Long categoryId, Long subCategoryId, Product.Status status,
            Pageable pageable) {
        return productRepository.search(keyword, categoryId, subCategoryId, status, pageable);
    }

    @Transactional
    public Product registerProduct(RegisterProductCommand command) {
        return productRepository.insert(command.subCategoryId(), command.modelName(),
                command.modelCode(), command.memo());
    }

    @Transactional
    public Product changeStatus(long id, Product.Status status) {
        return productRepository.changeStatus(id, status);
    }

    @Transactional
    public Product modifyProduct(long id, RegisterProductCommand command) {
        return productRepository.modifyProduct(id, command);
    }
}
