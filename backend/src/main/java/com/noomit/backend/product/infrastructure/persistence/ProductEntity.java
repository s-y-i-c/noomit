package com.noomit.backend.product.infrastructure.persistence;

import com.noomit.backend.product.domain.Product;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Getter
@Table(name = "product")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryEntity category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_category_id", nullable = false)
    private SubCategoryEntity subCategory;

    @Column(name = "model_name", nullable = false)
    private String modelName;

    @Column(name = "model_code", nullable = false, unique = true)
    private String modelCode;

    @Column(name = "memo")
    private String memo;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Product.Status status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    // 빌더를 직접 안 열고, 정적 팩토리 메소드로만 생성 허용
    public static ProductEntity create(SubCategoryEntity subCategory, String modelName,
                                       String modelCode, String memo) {
        ProductEntity product = new ProductEntity();
        product.subCategory = subCategory;
        product.category = subCategory.getCategory(); // category는 항상 subCategory에서 파생
        product.modelName = modelName;
        product.modelCode = modelCode;
        product.memo = memo;
        product.status = Product.Status.ACTIVE;
        return product;
    }

    // subCategory가 바뀌면 category도 항상 같이 바뀌도록 강제
    public void changeSubCategory(SubCategoryEntity subCategory) {
        this.subCategory = subCategory;
        this.category = subCategory.getCategory();
    }

    // 관리자가 판매중/단종 상태만 바꿀 때 쓴다.
    public void changeStatus(Product.Status status) {
        this.status = status;
    }

    public Product toDomain() {
        return new Product(id, category.getId(), subCategory.getId(), modelName, modelCode, memo, status, createdAt);
    }
}
