package com.noomit.backend.customer.infrastructure.persistence;

import java.util.Optional;
import com.noomit.backend.customer.domain.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface SpringDataCustomerRepository extends JpaRepository<CustomerEntity, Long> {

    // phone_number는 저장 시점(JpaCustomerRepository)에 이미 하이픈 없는 숫자만 남도록 정규화해서
    // 들어온다. 그래서 여기선 REPLACE() 없이 그대로 비교해도 되고, UNIQUE 인덱스도 그대로 탄다.
    // 호출하는 쪽에서 검색어의 하이픈은 미리 지워서 넘겨야 한다.
    Optional<CustomerEntity> findByPhoneNumber(String phoneNumber);

    // 이름은 "강 건"처럼 공백이 섞여 저장돼 있을 수 있어, 이름·검색어 둘 다 공백을 지우고 비교한다.
    // phoneNumber는 위와 같은 이유로 REPLACE() 없이 그대로 비교한다.
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
