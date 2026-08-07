package com.noomit.backend.customer.infrastructure.persistence;

import java.util.Optional;
import com.noomit.backend.customer.domain.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface SpringDataCustomerRepository extends JpaRepository<CustomerEntity, Long> {
    Optional<CustomerEntity> findByPhoneNumber(String phoneNumber);

    // 이름은 "강 건"처럼 공백이 섞여 저장돼 있을 수 있어, 이름·검색어 둘 다 공백을 지우고 비교한다.
    // status는 null이면 필터 없이 전체(활성+비활성) 대상.
    @Query("""
            SELECT c FROM CustomerEntity c
            WHERE (:keyword = ''
               OR REPLACE(LOWER(c.name), ' ', '') LIKE CONCAT('%', :keyword, '%')
               OR c.phoneNumber LIKE CONCAT('%', :keyword, '%'))
              AND (:status IS NULL OR c.status = :status)
            """)
    Page<CustomerEntity> search(@Param("keyword") String keyword, @Param("status") Customer.Status status,
            Pageable pageable);
}
