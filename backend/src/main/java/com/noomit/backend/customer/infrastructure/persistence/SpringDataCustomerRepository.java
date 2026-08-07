package com.noomit.backend.customer.infrastructure.persistence;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface SpringDataCustomerRepository extends JpaRepository<CustomerEntity, Long> {
    Optional<CustomerEntity> findByPhoneNumber(String phoneNumber);

    @Query("""
            SELECT c FROM CustomerEntity c
            WHERE :keyword = ''
               OR LOWER(c.name) LIKE CONCAT('%', :keyword, '%')
               OR c.phoneNumber LIKE CONCAT('%', :keyword, '%')
            """)
    Page<CustomerEntity> search(@Param("keyword") String keyword, Pageable pageable);
}
