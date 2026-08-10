package com.noomit.backend.statistics.infrastructure.product;

import java.util.Collection;
import java.util.List;
import com.noomit.backend.product.ProductDirectory;
import com.noomit.backend.statistics.application.port.ProductStatisticsReader;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/** 기존 제품 공개 조회 계약을 통계 출력 포트로 연결합니다. */
@Component
@RequiredArgsConstructor
class ProductStatisticsAdapter implements ProductStatisticsReader {
    private final ObjectProvider<ProductDirectory> directoryProvider;

    @Override
    public List<ProductSnapshot> readProducts(Collection<Long> productIds) {
        ProductDirectory directory = directoryProvider.getIfUnique();
        if (directory == null || productIds.isEmpty()) return List.of();
        return directory.findByIds(productIds).values().stream()
                .map(item -> new ProductSnapshot(item.id(), item.modelName()))
                .toList();
    }

    @Override
    public boolean connected() {
        return directoryProvider.getIfUnique() != null;
    }
}
