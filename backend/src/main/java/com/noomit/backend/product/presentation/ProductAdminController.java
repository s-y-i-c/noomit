package com.noomit.backend.product.presentation;

import com.noomit.backend.product.application.ProductService;
import com.noomit.backend.product.application.RegisterProductCommand;
import com.noomit.backend.product.domain.Product;
import com.noomit.backend.shared.ApiResponse;
import com.noomit.backend.shared.error.BusinessException;
import com.noomit.backend.shared.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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

    @PutMapping("/{id}/status")
    ApiResponse<Void> changeStatus(@PathVariable String id, @RequestBody String status) {
        productService.changeStatus(parseId(id), parseStatus(status));
        return ApiResponse.success("상태가 변경되었습니다.", null);
    }

    @PutMapping("/{id}")
    ApiResponse<ProductResponse> modifyProduct(@PathVariable String id, @RequestBody ProductRequest request) {
        Product result = productService.modifyProduct(parseId(id), new RegisterProductCommand(
                request.subCategoryId(), request.modelName(), request.modelCode(), request.memo()));
        return ApiResponse.success("제품을 수정했습니다.", ProductResponse.from(result));
    }

    private Product.Status parseStatus(String value) {
        try {
            return Product.Status.from(value);
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "상태 값이 올바르지 않습니다: " + value);
        }
    }

    private long parseId(String value) {
        try {
            long id = Long.parseLong(value);
            if (id <= 0) throw new NumberFormatException();
            return id;
        } catch (NumberFormatException exception) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "제품을 찾을 수 없습니다.");
        }
    }

    record ProductRequest(long subCategoryId, String modelName, String modelCode, String memo) {
    }
}
