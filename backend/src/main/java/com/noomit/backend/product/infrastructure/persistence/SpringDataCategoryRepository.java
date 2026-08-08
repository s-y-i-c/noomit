package com.noomit.backend.product.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataCategoryRepository extends JpaRepository<CategoryEntity, Long> {
}
