package com.noomit.backend.product.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataSubCategoryRepository extends JpaRepository<SubCategoryEntity, Long> {
}
