package com.noomit.backend.reception.application.product;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/** reception이 소비하는 제품 조회 창구. 임시 */
public interface ProductQueryPort {
    Optional<ProductInfo> getProduct(Long productId);

    Map<Long, ProductInfo> getProducts(List<Long> productIds);
}