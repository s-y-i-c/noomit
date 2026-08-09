package com.noomit.backend.product.infrastructure.persistence;

import java.util.Optional;
import com.noomit.backend.product.application.ProductRepository;
import com.noomit.backend.product.domain.Product;
import com.noomit.backend.shared.error.BusinessException;
import com.noomit.backend.shared.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
class JpaProductRepository implements ProductRepository {
    private final SpringDataProductRepository products;
    private final SpringDataSubCategoryRepository subCategories;

    @Override
    public Optional<Product> findById(long id) {
        return products.findById(id).map(ProductEntity::toDomain);
    }

    @Override
    public Product insert(long subCategoryId, String modelName, String modelCode, String memo) {
        SubCategoryEntity subCategory = subCategories.findById(subCategoryId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.PRODUCT_SUB_CATEGORY_NOT_FOUND, "서브카테고리를 찾을 수 없습니다."));
        ProductEntity entity = ProductEntity.create(subCategory, modelName, modelCode, memo);
        try {
            return products.save(entity).toDomain();
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessException(
                    ErrorCode.PRODUCT_MODEL_CODE_ALREADY_EXISTS, "이미 등록된 모델코드입니다.");
        }
    }
}
