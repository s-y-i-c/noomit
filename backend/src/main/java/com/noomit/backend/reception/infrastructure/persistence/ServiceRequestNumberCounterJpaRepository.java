package com.noomit.backend.reception.infrastructure.persistence;

import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface ServiceRequestNumberCounterJpaRepository extends JpaRepository<ServiceRequestNumberCounterEntity, LocalDate> {

    @Query(value = """
            INSERT INTO service_request_number_counter (request_date, last_seq)
            VALUES (:requestDate, 1)
            ON CONFLICT (request_date)
            DO UPDATE SET last_seq = service_request_number_counter.last_seq + 1
            RETURNING last_seq
            """, nativeQuery = true)
    int incrementAndGet(@Param("requestDate") LocalDate requestDate);
}