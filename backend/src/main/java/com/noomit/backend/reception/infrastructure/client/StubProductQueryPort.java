package com.noomit.backend.reception.infrastructure.client;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.noomit.backend.reception.application.product.ProductInfo;
import com.noomit.backend.reception.application.product.ProductQueryPort;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

// TODO: product 모듈에 실제 ProductQueryPort 구현체가 생기면 이 클래스를 삭제한다.
@Component
@Profile("!prod")
public class StubProductQueryPort implements ProductQueryPort {

    @Override
    public ProductInfo getProduct(Long productId) {
        return new ProductInfo(productId, "제품" + productId);
    }

    @Override
    public Map<Long, ProductInfo> getProducts(List<Long> productIds) {
        return productIds.stream()
                .collect(Collectors.toMap(id -> id, id -> new ProductInfo(id, "제품" + id)));
    }
}