package com.noomit.backend.product.application;

import java.util.Optional;
import com.noomit.backend.product.domain.Product;
import org.springframework.data.domain.Pageable;

public interface ProductRepository {
    Optional<Product> findById(long id);

    /** 모델명·모델코드 부분 검색 + 카테고리/서브카테고리/상태 필터. 각 필터는 null이면 조건 없음(status는 전체 상태). */
    ProductPage search(String keyword, Long categoryId, Long subCategoryId, Product.Status status, Pageable pageable);

    Product insert(long subCategoryId, String modelName, String modelCode, String memo);

    Product changeStatus(long id, Product.Status status);
}
