package com.noomit.backend.reception.application;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

// 날짜별 카운터 행의 row lock을 접수 생성 트랜잭션 전체가 아니라 
// 이 채번 연산 하나만큼만 짧게 쥐도록 별도 트랜잭션 경계로 분리.
@Service
@RequiredArgsConstructor
class ServiceRequestNumberService {
    private static final ZoneId REQUEST_NUMBER_ZONE = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter REQUEST_NUMBER_DATE_FORMAT = DateTimeFormatter.ofPattern("yyMMdd");

    private final ServiceRequestNumberRepository requestNumberRepository;

    @Transactional(transactionManager = "requestNumberTransactionManager", propagation = Propagation.REQUIRES_NEW)
    String issue(Instant requestedAt) {
        LocalDate requestDate = requestedAt.atZone(REQUEST_NUMBER_ZONE).toLocalDate();
        int seq = requestNumberRepository.issueSequence(requestDate);
        return "RCP-" + requestDate.format(REQUEST_NUMBER_DATE_FORMAT) + "-" + String.format("%04d", seq);
    }
}