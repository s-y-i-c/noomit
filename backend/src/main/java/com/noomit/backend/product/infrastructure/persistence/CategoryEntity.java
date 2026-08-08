package com.noomit.backend.product.infrastructure.persistence;

import com.noomit.backend.product.domain.Category;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "category")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CategoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    public Category toDomain() {
        return new Category(id, name);
    }
}
