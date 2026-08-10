package com.noomit.backend.product.presentation;

import com.noomit.backend.product.application.ProductPage;
import com.noomit.backend.product.application.ProductService;
import com.noomit.backend.product.domain.Category;
import com.noomit.backend.product.domain.Product;
import com.noomit.backend.product.domain.SubCategory;
import com.noomit.backend.shared.ApiResponse;
import com.noomit.backend.shared.error.BusinessException;
import com.noomit.backend.shared.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @GetMapping("/categories")
    ApiResponse<List<CategoryResponse>> categoryList(){
        List<CategoryResponse> categories = productService.getCategoryList().stream()
                .map(CategoryResponse::from)
                .toList();
        return ApiResponse.success(categories);
    }

    // 카테고리별로 나눠 받지 않고 전체를 한 번에 내려준다. 프론트에서 categoryId로 묶어 쓰면 된다.
    @GetMapping("/subcategories")
    ApiResponse<List<SubCategoryResponse>> subCategoryList(){
        List<SubCategoryResponse> subCategories = productService.getSubCategoryList().stream()
                .map(SubCategoryResponse::from)
                .toList();
        return ApiResponse.success(subCategories);
    }

    // ?page=0&size=20&sort=modelName&keyword=모델명 또는 모델코드&categoryId=1&subCategoryId=3&status=ACTIVE(또는 INACTIVE, 생략 시 전체)
    @GetMapping
    ApiResponse<ProductPageResponse> listProducts(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long subCategoryId,
            @RequestParam(required = false) String status,
            @PageableDefault(sort = "modelName") Pageable pageable) {
        Product.Status parsedStatus = parseStatusOrNull(status);
        ProductPage result = productService.list(keyword, categoryId, subCategoryId, parsedStatus, pageable);
        return ApiResponse.success(ProductPageResponse.from(result));
    }

    @GetMapping("/{id}")
    ApiResponse<ProductResponse> getProduct(@PathVariable String id) {
        Product product = productService.findById(parseId(id));
        return ApiResponse.success(ProductResponse.from(product));
    }

    private Product.Status parseStatusOrNull(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Product.Status.from(value);
        } catch (IllegalArgumentException exception) {
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

    record CategoryResponse(String id, String name){
        static CategoryResponse from(Category category){
            return new CategoryResponse(Long.toString(category.id()), category.name());
        }
    }

    record SubCategoryResponse(String id, String categoryId, String name){
        static SubCategoryResponse from(SubCategory subCategory){
            return new SubCategoryResponse(Long.toString(subCategory.id()),
                    Long.toString(subCategory.categoryId()), subCategory.name());
        }
    }

    record ProductPageResponse(List<ProductResponse> products, int page, long totalElements, int totalPages) {
        static ProductPageResponse from(ProductPage page) {
            List<ProductResponse> products = page.products().stream()
                    .map(ProductResponse::from).toList();
            return new ProductPageResponse(products, page.page(), page.totalElements(), page.totalPages());
        }
    }
}
