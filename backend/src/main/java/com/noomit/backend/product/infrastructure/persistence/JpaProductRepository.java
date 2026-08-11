package com.noomit.backend.product.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import com.noomit.backend.product.application.ProductPage;
import com.noomit.backend.product.application.ProductRepository;
import com.noomit.backend.product.application.RegisterProductCommand;
import com.noomit.backend.product.domain.Product;
import com.noomit.backend.shared.error.BusinessException;
import com.noomit.backend.shared.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public ProductPage search(String keyword, Long categoryId, Long subCategoryId, Product.Status status,
            Pageable pageable) {
        String verifiedKeyword = keyword == null ? "" : keyword.toLowerCase();
        Page<ProductEntity> result = products.search(verifiedKeyword, categoryId, subCategoryId, status, pageable);
        List<Product> found = result.stream().map(ProductEntity::toDomain).toList();
        return new ProductPage(found, pageable.getPageNumber(), result.getTotalElements(), result.getTotalPages());
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

    @Override
    public Product changeStatus(long id, Product.Status status) {
        ProductEntity entity = products.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "제품을 찾을 수 없습니다."));
        entity.changeStatus(status);
        return entity.toDomain();
    }

    @Override
    public Product modifyProduct(long id, RegisterProductCommand command) {
        ProductEntity entity = products.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "제품을 찾을 수 없습니다."));
        SubCategoryEntity subCategory = subCategories.findById(command.subCategoryId())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.PRODUCT_SUB_CATEGORY_NOT_FOUND, "서브카테고리를 찾을 수 없습니다."));
        entity.modify(subCategory, command.modelName(), command.modelCode(), command.memo());
        try {
            // saveAndFlush로 즉시 flush해야 unique 제약 위반이 여기서 바로 잡힌다.
            // (그냥 필드만 바꾸면 트랜잭션 커밋 시점에야 UPDATE가 나가서 여기서 못 잡음)
            return products.saveAndFlush(entity).toDomain();
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessException(
                    ErrorCode.PRODUCT_MODEL_CODE_ALREADY_EXISTS, "이미 등록된 모델코드입니다.");
        }
    }
}
