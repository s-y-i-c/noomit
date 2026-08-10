package com.noomit.backend.repair.application;

import com.noomit.backend.repair.application.port.RepairCaseRepository;
import com.noomit.backend.repair.domain.RepairCase;
import com.noomit.backend.repair.domain.RepairStatus;
import com.noomit.backend.shared.error.BusinessException;
import com.noomit.backend.shared.error.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Testcontainers
@Transactional
class RepairCaseServiceIntegrationTest {

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
    RepairCaseService service;

    @Autowired
    RepairCaseRepository repairCaseRepository;

    // ── 픽스처 ────────────────────────────────────────────────────────────────

    private RepairCase createCase(long serviceRequestId, long technicianId) {
        return repairCaseRepository.create(serviceRequestId, technicianId);
    }

    // ── 수리 케이스 생성 ─────────────────────────────────────────────────────
    // @ApplicationModuleListener 는 Spring Modulith에 의해 비동기 실행됨.
    // 이벤트 리스너 동작(멱등성 가드)은 단위 테스트에서 검증.
    // 통합 테스트에서는 실제 DB 저장 및 조회 정확성을 검증.

    @Nested
    @DisplayName("수리 케이스 생성")
    class CreateRepairCase {

        @Test
        @DisplayName("create() 시 RepairCase가 IN_PROGRESS 상태, totalAmount=0으로 저장된다")
        void create_시_케이스가_IN_PROGRESS로_저장된다() {
            // Act
            RepairCase created = repairCaseRepository.create(1L, 10L);

            // Assert
            assertThat(created.id()).isNotNull();
            assertThat(created.status()).isEqualTo(RepairStatus.IN_PROGRESS);
            assertThat(created.totalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(created.rejectReason()).isNull();
            assertThat(created.createdAt()).isNotNull();
        }

        @Test
        @DisplayName("create() 후 existsByServiceRequestId()가 true를 반환한다")
        void create_후_exist_체크가_true를_반환한다() {
            // Arrange
            assertThat(repairCaseRepository.existsByServiceRequestId(2L)).isFalse();

            // Act
            repairCaseRepository.create(2L, 10L);

            // Assert
            assertThat(repairCaseRepository.existsByServiceRequestId(2L)).isTrue();
        }
    }

    // ── 수리 내역 추가 / 삭제 ─────────────────────────────────────────────────

    @Nested
    @DisplayName("수리 내역 추가")
    class AddDetail {

        @Test
        @DisplayName("내역 추가 후 totalAmount가 해당 금액으로 갱신된다")
        void 내역_추가_후_총액이_갱신된다() {
            // Arrange
            RepairCase repairCase = createCase(10L, 20L);

            // Act
            RepairCase result = service.addDetail(repairCase.id(), 20L, "부품 교체", BigDecimal.valueOf(30000));

            // Assert
            assertThat(result.details()).hasSize(1);
            assertThat(result.totalAmount()).isEqualByComparingTo(BigDecimal.valueOf(30000));
        }

        @Test
        @DisplayName("내역을 여러 개 추가하면 totalAmount는 합산된다")
        void 내역_여러개_추가시_총액이_합산된다() {
            // Arrange
            RepairCase repairCase = createCase(11L, 20L);

            // Act
            service.addDetail(repairCase.id(), 20L, "부품 교체", BigDecimal.valueOf(30000));
            RepairCase result = service.addDetail(repairCase.id(), 20L, "공임비", BigDecimal.valueOf(20000));

            // Assert
            assertThat(result.details()).hasSize(2);
            assertThat(result.totalAmount()).isEqualByComparingTo(BigDecimal.valueOf(50000));
        }
    }

    @Nested
    @DisplayName("수리 내역 삭제")
    class DeleteDetail {

        @Test
        @DisplayName("내역 삭제 후 totalAmount가 재계산된다")
        void 내역_삭제_후_총액이_재계산된다() {
            // Arrange
            RepairCase repairCase = createCase(12L, 20L);
            service.addDetail(repairCase.id(), 20L, "부품 교체", BigDecimal.valueOf(30000));
            RepairCase afterAdd = service.addDetail(repairCase.id(), 20L, "공임비", BigDecimal.valueOf(20000));
            long detailIdToDelete = afterAdd.details().get(0).id();

            // Act
            service.deleteDetail(repairCase.id(), detailIdToDelete, 20L);

            // Assert
            RepairCase afterDelete = service.getRepairCase(repairCase.id(), 20L);
            assertThat(afterDelete.details()).hasSize(1);
            assertThat(afterDelete.totalAmount()).isEqualByComparingTo(BigDecimal.valueOf(20000));
        }

        @Test
        @DisplayName("내역을 모두 삭제하면 totalAmount가 0이 된다 (COALESCE 검증)")
        void 내역_전체_삭제시_총액이_0이_된다() {
            // Arrange
            RepairCase repairCase = createCase(13L, 20L);
            RepairCase afterAdd = service.addDetail(repairCase.id(), 20L, "부품 교체", BigDecimal.valueOf(30000));
            long detailId = afterAdd.details().get(0).id();

            // Act
            service.deleteDetail(repairCase.id(), detailId, 20L);

            // Assert — COALESCE(SUM(...), 0)이 제대로 동작해야 0 반환
            RepairCase afterDelete = service.getRepairCase(repairCase.id(), 20L);
            assertThat(afterDelete.details()).isEmpty();
            assertThat(afterDelete.totalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    // ── 상태 전이 ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("완료 제출")
    class Submit {

        @Test
        @DisplayName("submit() 후 상태가 SUBMITTED로 저장된다")
        void 제출_후_상태가_SUBMITTED로_저장된다() {
            // Arrange
            RepairCase repairCase = createCase(20L, 30L);
            service.addDetail(repairCase.id(), 30L, "부품 교체", BigDecimal.valueOf(10000));

            // Act
            RepairCase submitted = service.submit(repairCase.id(), 30L);

            // Assert
            assertThat(submitted.status()).isEqualTo(RepairStatus.SUBMITTED);
            assertThat(service.getMyRepairCases(30L).get(0).status()).isEqualTo(RepairStatus.SUBMITTED);
        }
    }

    @Nested
    @DisplayName("완료 승인 (관리자용)")
    class Approve {

        @Test
        @DisplayName("approve() 후 상태가 COMPLETED로 저장된다")
        void 승인_후_상태가_COMPLETED로_저장된다() {
            // Arrange
            RepairCase repairCase = createCase(21L, 30L);
            service.addDetail(repairCase.id(), 30L, "부품 교체", BigDecimal.valueOf(10000));
            service.submit(repairCase.id(), 30L);

            // Act
            RepairCase completed = service.approve(repairCase.id());

            // Assert
            assertThat(completed.status()).isEqualTo(RepairStatus.COMPLETED);
        }
    }

    @Nested
    @DisplayName("완료 반려 (관리자용)")
    class Reject {

        @Test
        @DisplayName("reject() 후 상태가 IN_PROGRESS로 돌아오고 rejectReason이 저장된다")
        void 반려_후_상태가_IN_PROGRESS로_돌아오고_사유가_저장된다() {
            // Arrange
            RepairCase repairCase = createCase(22L, 30L);
            service.addDetail(repairCase.id(), 30L, "부품 교체", BigDecimal.valueOf(10000));
            service.submit(repairCase.id(), 30L);

            // Act
            RepairCase rejected = service.reject(repairCase.id(), "부품 누락");

            // Assert
            assertThat(rejected.status()).isEqualTo(RepairStatus.IN_PROGRESS);
            assertThat(rejected.rejectReason()).isEqualTo("부품 누락");
        }

        @Test
        @DisplayName("반려 후 기사가 내역 수정 후 다시 제출할 수 있다")
        void 반려_후_재제출이_가능하다() {
            // Arrange
            RepairCase repairCase = createCase(23L, 30L);
            RepairCase afterAdd = service.addDetail(repairCase.id(), 30L, "부품 교체", BigDecimal.valueOf(10000));
            service.submit(repairCase.id(), 30L);
            service.reject(repairCase.id(), "부품 누락");

            // Act — 새 내역 추가 후 재제출
            service.addDetail(repairCase.id(), 30L, "누락 부품 추가", BigDecimal.valueOf(5000));
            RepairCase resubmitted = service.submit(repairCase.id(), 30L);

            // Assert
            assertThat(resubmitted.status()).isEqualTo(RepairStatus.SUBMITTED);
        }
    }

    // ── 조회 ──────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("목록 조회")
    class FindCases {

        @Test
        @DisplayName("getAllRepairCases() 상태 필터로 해당 상태 케이스만 반환한다")
        void 상태_필터로_해당_상태만_반환된다() {
            // Arrange
            RepairCase inProgress = createCase(30L, 40L);
            RepairCase toSubmit = createCase(31L, 40L);
            service.addDetail(toSubmit.id(), 40L, "부품 교체", BigDecimal.valueOf(10000));
            service.submit(toSubmit.id(), 40L);

            // Act
            List<RepairCase> submittedList = service.getAllRepairCases(RepairStatus.SUBMITTED);
            List<RepairCase> inProgressList = service.getAllRepairCases(RepairStatus.IN_PROGRESS);

            // Assert
            assertThat(submittedList).anyMatch(c -> c.id().equals(toSubmit.id()));
            assertThat(submittedList).noneMatch(c -> c.id().equals(inProgress.id()));
            assertThat(inProgressList).anyMatch(c -> c.id().equals(inProgress.id()));
        }

        @Test
        @DisplayName("findByTechnicianId() 는 해당 기사의 케이스에 details를 함께 반환한다 (배치 조회)")
        void 기사별_케이스_조회시_상세내역이_함께_반환된다() {
            // Arrange
            RepairCase repairCase = createCase(32L, 50L);
            service.addDetail(repairCase.id(), 50L, "부품 교체", BigDecimal.valueOf(10000));
            service.addDetail(repairCase.id(), 50L, "공임비", BigDecimal.valueOf(5000));

            // Act
            List<RepairCase> cases = service.getMyRepairCases(50L);

            // Assert — details가 배치 IN 쿼리로 함께 로딩됨
            assertThat(cases).hasSize(1);
            assertThat(cases.get(0).details()).hasSize(2);
        }

        @Test
        @DisplayName("존재하지 않는 케이스 조회 시 REPAIR_CASE_NOT_FOUND 예외")
        void 존재하지_않는_케이스_조회시_예외() {
            assertThatThrownBy(() -> service.getRepairCase(9999L, 1L))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).errorCode())
                    .isEqualTo(ErrorCode.REPAIR_CASE_NOT_FOUND);
        }
    }
}
