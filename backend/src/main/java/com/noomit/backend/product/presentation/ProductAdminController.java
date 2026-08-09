package com.noomit.backend.product.presentation;

import com.noomit.backend.product.application.ProductService;
import com.noomit.backend.product.application.RegisterProductCommand;
import com.noomit.backend.product.domain.Product;
import com.noomit.backend.shared.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class ProductAdminController {
    private final ProductService productService;

    @PostMapping
    ApiResponse<ProductResponse> register(@RequestBody ProductRequest request) {
        Product result = productService.registerProduct(new RegisterProductCommand(
                request.subCategoryId(), request.modelName(), request.modelCode(), request.memo()));
        return ApiResponse.success("제품을 등록했습니다.", ProductResponse.from(result));
    }

    record ProductRequest(long subCategoryId, String modelName, String modelCode, String memo) {
    }

    record ProductResponse(String id, long categoryId, long subCategoryId, String modelName,
                           String modelCode, String memo) {
        static ProductResponse from(Product product) {
            return new ProductResponse(Long.toString(product.id()), product.categoryId(), product.subCategoryId(),
                    product.modelName(), product.modelCode(), product.memo());
        }
    }
}
