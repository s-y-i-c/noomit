package com.noomit.backend.reception.infrastructure.persistence;

import java.time.LocalDate;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 날짜별 접수번호 채번 카운터 테이블
 */
@Entity
@Table(name = "service_request_number_counter")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class ServiceRequestNumberCounterEntity {
    @Id
    @Column(name = "request_date")
    private LocalDate requestDate;

    @Column(name = "last_seq", nullable = false)
    private Integer lastSeq;
}