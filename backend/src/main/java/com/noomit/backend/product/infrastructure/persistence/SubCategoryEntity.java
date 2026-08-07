package com.noomit.backend.product.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "sub_category")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SubCategoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sub_category_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryEntity category;

    @Column(name = "name", nullable = false)
    private String name;
}
