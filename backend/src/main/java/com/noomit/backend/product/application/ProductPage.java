package com.noomit.backend.product.application;

import java.util.List;
import com.noomit.backend.product.domain.Product;

/** 제품 목록 한 페이지. */
public record ProductPage(List<Product> products, int page, long totalElements, int totalPages) {
}
