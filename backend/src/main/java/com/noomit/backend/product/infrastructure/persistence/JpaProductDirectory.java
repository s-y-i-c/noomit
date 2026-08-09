package com.noomit.backend.product.infrastructure.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.noomit.backend.product.ProductDirectory;
import com.noomit.backend.product.ProductInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
class JpaProductDirectory implements ProductDirectory {
    private final SpringDataProductRepository products;

    @Override
    public Optional<ProductInfo> findById(long productId) {
        return products.findById(productId).map(JpaProductDirectory::toInfo);
    }

    @Override
    public Map<Long, ProductInfo> findByIds(Collection<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) return Map.of();
        List<Long> distinctIds = productIds.stream().distinct().toList();
        return products.findAllById(distinctIds).stream()
                .map(JpaProductDirectory::toInfo)
                .collect(Collectors.toMap(ProductInfo::id, Function.identity()));
    }

    private static ProductInfo toInfo(ProductEntity entity) {
        return new ProductInfo(entity.getId(), entity.getModelName());
    }
}
