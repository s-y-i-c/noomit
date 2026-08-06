package com.noomit.backend.statistics.application.port;

import java.util.Collection;
import java.util.List;

/** 제품 이름은 N+1 조회를 피하도록 ID 집합으로 한 번에 요청한다. */
public interface ProductStatisticsReader {
    List<ProductSnapshot> readProducts(Collection<String> productIds);

    default boolean connected() {
        return true;
    }

    record ProductSnapshot(String productId, String productName) {}
}
