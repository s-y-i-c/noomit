package com.noomit.backend.product.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataProductRepository extends JpaRepository<ProductEntity, Long> {
}
