package com.noomit.backend.product.infrastructure.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.noomit.backend.product.ProductDirectory;
import com.noomit.backend.product.ProductInfo;
import com.noomit.backend.product.SubCategoryInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
class JpaProductDirectory implements ProductDirectory {
    private final SpringDataProductRepository products;
    private final SpringDataSubCategoryRepository subCategories;

    @Override
    public Optional<ProductInfo> findById(long productId) {
        return products.findById(productId).map(JpaProductDirectory::toProductInfo);
    }

    @Override
    public Map<Long, ProductInfo> findByIds(Collection<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) return Map.of();
        List<Long> distinctIds = productIds.stream().distinct().toList();
        return products.findAllById(distinctIds).stream()
                .map(JpaProductDirectory::toProductInfo)
                .collect(Collectors.toMap(ProductInfo::id, Function.identity()));
    }

    @Override
    public Map<Long, SubCategoryInfo> findSubCategoriesByIds(Collection<Long> subCategoryIds) {
        if (subCategoryIds == null || subCategoryIds.isEmpty()) return Map.of();
        List<Long> distinctIds = subCategoryIds.stream().distinct().toList();
        return subCategories.findAllById(distinctIds).stream()
                .map(JpaProductDirectory::toSubCategoryInfo)
                .collect(Collectors.toMap(SubCategoryInfo::id, Function.identity()));
    }

    private static ProductInfo toProductInfo(ProductEntity entity) {
        return new ProductInfo(entity.getId(), entity.getModelName());
    }

    private static SubCategoryInfo toSubCategoryInfo(SubCategoryEntity entity) {
        return new SubCategoryInfo(entity.getId(), entity.getName());
    }
}
