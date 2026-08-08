package com.noomit.backend.product.application;

import com.noomit.backend.product.domain.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final CategoryRepository categoryRepository;
    public List<Category> getCategoryList() {
        return categoryRepository.findAllCategories();
    }
}
