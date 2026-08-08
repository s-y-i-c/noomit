package com.noomit.backend.product.presentation;

import com.noomit.backend.product.application.ProductService;
import com.noomit.backend.product.domain.Category;
import com.noomit.backend.shared.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    record CategoryResponse(String name){
        static CategoryResponse from(Category category){
            return new CategoryResponse(category.name());
        }
    }
}
