package com.noomit.backend.product.presentation;

import com.noomit.backend.product.application.ProductService;
import com.noomit.backend.product.domain.Category;
import com.noomit.backend.product.domain.SubCategory;
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

    // 카테고리별로 나눠 받지 않고 전체를 한 번에 내려준다. 프론트에서 categoryId로 묶어 쓰면 된다.
    @GetMapping("/subcategories")
    ApiResponse<List<SubCategoryResponse>> subCategoryList(){
        List<SubCategoryResponse> subCategories = productService.getSubCategoryList().stream()
                .map(SubCategoryResponse::from)
                .toList();
        return ApiResponse.success(subCategories);
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
}
