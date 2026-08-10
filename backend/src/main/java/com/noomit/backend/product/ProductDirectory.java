package com.noomit.backend.product;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * product 모듈이 다른 모듈에 공개하는 조회 창구.
 *
 * <p>다른 모듈이 product 테이블을 직접 읽으면 모듈 경계가 무너지므로, 이 인터페이스만 의존하게 한다.
 * 접수(reception) 목록·상세 화면처럼 제품 모델명만 필요한 화면 표시용이다.</p>
 */
public interface ProductDirectory {
    /** 단건 조회. 제품이 없거나 삭제된 경우 빈 값을 반환한다. */
    Optional<ProductInfo> findById(long productId);

    /** 배치 조회. 목록 화면에서 N+1 없이 한 번에 가져오라고 Map으로 돌려준다. 없는 id는 그냥 빠진다. */
    Map<Long, ProductInfo> findByIds(Collection<Long> productIds);

    Map<Long, SubCategoryInfo> findSubCategoriesByIds(Collection<Long> subCategoryIds);
}
