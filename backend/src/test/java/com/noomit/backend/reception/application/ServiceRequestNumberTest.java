package com.noomit.backend.reception.application;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import com.noomit.backend.customer.CustomerDirectory;
import com.noomit.backend.reception.domain.ServiceRequest;
import com.noomit.backend.user.UserDirectory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 접수번호 채번 통합 테스트.
 *
 * 접수번호는 requestedAt을 Asia/Seoul 기준 LocalDate로 변환하여 날짜별 순번을 부여
 * 자정 경계, 타임존 경계 및 동시 생성 상황에서 접수번호가 정확하고 중복 없이 발급되는지 검증
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Testcontainers
@Transactional
class ServiceRequestNumberTest {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("noomit_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    ServiceRequestRepository requestRepository;

    @Autowired
    TechnicianAvailabilityRepository availabilityRepository;

    @Autowired
    UserDirectory userDirectory;

    @Autowired
    CustomerDirectory customerDirectory;

    @Autowired
    ServiceRequestNumberService requestNumberService;

    @Autowired
    ApplicationEventPublisher eventPublisher;

    @Autowired
    JdbcTemplate jdbcTemplate;
    
    @BeforeTransaction
    void resetRequestNumberCounter() {
        jdbcTemplate.update(
                "DELETE FROM service_request_number_counter WHERE request_date IN (?, ?, ?)",
                LocalDate.of(2026, 8, 11), LocalDate.of(2026, 8, 12), LocalDate.of(2026, 8, 15));
    }

    private ServiceRequest createAt(Instant instant, String phoneNumber) {
        ServiceRequestService service = new ServiceRequestService(
                requestRepository, availabilityRepository, userDirectory, customerDirectory,
                requestNumberService, eventPublisher, Clock.fixed(instant, KST));

        CreateServiceRequestCommand command = new CreateServiceRequestCommand(
                "테스트고객", phoneNumber, "12345", "서울시 어딘가", "101동", null,
                1L, null, null, 1L, "증상", null);

        return service.create(command);
    }

    private String seqOf(String requestNumber) {
        return requestNumber.substring(requestNumber.length() - 4);
    }

    @Test
    @DisplayName("KST 자정을 넘으면 접수번호 날짜가 변경되고 순번이 0001부터 시작")
    void KST_자정을_넘으면_접수번호_날짜와_순번이_초기화() {
        // 2026-08-11 23:59:59.9 KST
        Instant beforeMidnight = ZonedDateTime.of(2026, 8, 11, 23, 59, 59, 900_000_000, KST).toInstant();
        // 2026-08-12 00:00:00.1 KST
        Instant afterMidnight = ZonedDateTime.of(2026, 8, 12, 0, 0, 0, 100_000_000, KST).toInstant();

        ServiceRequest last0811 = createAt(beforeMidnight, "01011110001");
        ServiceRequest first0812 = createAt(afterMidnight, "01011110002");

        assertThat(last0811.requestNumber()).startsWith("RCP-260811-");
        assertThat(first0812.requestNumber()).startsWith("RCP-260812-");
        assertThat(seqOf(first0812.requestNumber())).isEqualTo("0001");
    }

    @Test
    @DisplayName("같은 날짜 안에서는 자정 근처에서도 순번이 끊기지 않고 이어짐")
    void 같은_KST_날짜에서는_순번이_연속() {
        // 23:59:58
        Instant t1 = ZonedDateTime.of(2026, 8, 11, 23, 59, 58, 0, KST).toInstant();
        // 23:59:59
        Instant t2 = ZonedDateTime.of(2026, 8, 11, 23, 59, 59, 0, KST).toInstant();

        ServiceRequest r1 = createAt(t1, "01022220001");
        ServiceRequest r2 = createAt(t2, "01022220002");

        assertThat(r1.requestNumber()).startsWith("RCP-260811-");
        assertThat(r2.requestNumber()).startsWith("RCP-260811-");
        assertThat(Integer.parseInt(seqOf(r2.requestNumber())))
                .isEqualTo(Integer.parseInt(seqOf(r1.requestNumber())) + 1);
    }

    @Test
    @DisplayName("KST로는 자정을 넘었지만 UTC로는 같은 날인 경계에서도 KST 날짜 기준으로 채번")
    void UTC_날짜와_달라도_KST_기준으로_접수번호_채번() {
        // UTC 기준 2026-08-11 15:00:00 == KST 2026-08-12 00:00:00 (UTC+9)
        Instant justAfterKstMidnight = Instant.parse("2026-08-11T15:00:00Z");

        ServiceRequest request = createAt(justAfterKstMidnight, "01033330001");

        assertThat(request.requestNumber()).startsWith("RCP-260812-");
    }

    // 동시성 테스트에서는 여러 워커 스레드가 각각 독립적으로 DB 작업을 수행해야 하므로
    // 클래스 레벨 @Transactional을 적용하지 않고 실제 DB 트랜잭션 환경에서 검증
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @DisplayName("같은 시각에 여러 접수가 동시에 생성돼도 순번이 0001,0002,...로 중복 없이 채번")
    void 동시_생성시_순번이_중복없이_채번() throws Exception {
        Instant sameInstant = ZonedDateTime.of(2026, 8, 15, 10, 0, 0, 0, KST).toInstant();
        int threadCount = 8;

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<String>> futures = new ArrayList<>(); // 결과 저장

        try {
            for (int i = 0; i < threadCount; i++) {
                String phoneNumber = "010" + String.format("%08d", i);
                futures.add(executor.submit(() -> {
                    ready.countDown();
                    start.await();
                    return createAt(sameInstant, phoneNumber).requestNumber();
                }));
            }

            ready.await(); // 8개의 워커 준비될 때까지 기다리기
            start.countDown(); // 8개 동시 접수 생성 시작

            List<String> requestNumbers = new ArrayList<>();
            for (Future<String> future : futures) {
                requestNumbers.add(future.get(30, TimeUnit.SECONDS));
            }

            assertThat(requestNumbers).hasSize(threadCount);
            assertThat(requestNumbers).allMatch(n -> n.startsWith("RCP-260815-"));
            assertThat(new HashSet<>(requestNumbers)).hasSize(threadCount); // 중복 없음 검증

            List<String> seqs = requestNumbers.stream().map(this::seqOf).sorted().toList(); // 순번 뽑기
            assertThat(seqs).containsExactly(
                    "0001", "0002", "0003", "0004", "0005", "0006", "0007", "0008"); // 순번 중복 없이 연속 검증
        } finally {
            executor.shutdown();
        }
    }
}