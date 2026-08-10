package com.noomit.backend.product.infrastructure.persistence;

import com.noomit.backend.product.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface SpringDataProductRepository extends JpaRepository<ProductEntity, Long> {

    // keyword는 모델명·모델코드 부분 일치(대소문자 무시). categoryId/subCategoryId/status는 null이면 조건 없음.
    @Query("""
            SELECT p FROM ProductEntity p
            WHERE (:keyword = ''
               OR LOWER(p.modelName) LIKE CONCAT('%', :keyword, '%')
               OR LOWER(p.modelCode) LIKE CONCAT('%', :keyword, '%'))
              AND (:categoryId IS NULL OR p.category.id = :categoryId)
              AND (:subCategoryId IS NULL OR p.subCategory.id = :subCategoryId)
              AND (:status IS NULL OR p.status = :status)
            """)
    Page<ProductEntity> search(@Param("keyword") String keyword, @Param("categoryId") Long categoryId,
            @Param("subCategoryId") Long subCategoryId, @Param("status") Product.Status status, Pageable pageable);
}
