package com.noomit.backend.repair.application;

import com.noomit.backend.reception.ServiceRequestAssigned;
import com.noomit.backend.repair.application.port.RepairCaseRepository;
import com.noomit.backend.repair.application.port.RepairDetailRepository;
import com.noomit.backend.repair.domain.RepairCase;
import com.noomit.backend.repair.domain.RepairDetail;
import com.noomit.backend.repair.domain.RepairStatus;
import com.noomit.backend.shared.error.BusinessException;
import com.noomit.backend.shared.error.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepairCaseServiceTest {

    @Mock RepairCaseRepository repairCaseRepository;
    @Mock RepairDetailRepository repairDetailRepository;
    @InjectMocks RepairCaseService service;

    // ── 픽스처 ────────────────────────────────────────────────────────────────

    private RepairCase caseWith(long id, long technicianId, RepairStatus status) {
        return new RepairCase(id, 100L, technicianId, status,
                BigDecimal.ZERO, null, OffsetDateTime.now(), OffsetDateTime.now(), List.of());
    }

    private RepairDetail detail(long id, long repairCaseId) {
        return new RepairDetail(id, repairCaseId, "부품 교체", BigDecimal.valueOf(30000), OffsetDateTime.now());
    }

    // ── onServiceRequestAssigned ──────────────────────────────────────────────

    @Nested
    @DisplayName("수리 케이스 생성 (이벤트 수신)")
    class OnServiceRequestAssigned {

        @Test
        @DisplayName("이미 케이스가 존재하면 생성하지 않는다 (멱등성)")
        void 이미_케이스가_존재하면_생성하지_않는다() {
            // Arrange
            ServiceRequestAssigned event = new ServiceRequestAssigned(1L, 2L, 3L, 4L, Instant.now());
            when(repairCaseRepository.existsByServiceRequestId(1L)).thenReturn(true);

            // Act
            service.onServiceRequestAssigned(event);

            // Assert
            verify(repairCaseRepository, never()).create(anyLong(), anyLong());
        }

        @Test
        @DisplayName("케이스가 없으면 새로 생성한다")
        void 케이스가_없으면_새로_생성한다() {
            // Arrange
            ServiceRequestAssigned event = new ServiceRequestAssigned(1L, 2L, 3L, 4L, Instant.now());
            when(repairCaseRepository.existsByServiceRequestId(1L)).thenReturn(false);

            // Act
            service.onServiceRequestAssigned(event);

            // Assert
            verify(repairCaseRepository).create(1L, 4L);
        }
    }

    // ── getRepairCase ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("수리 케이스 단건 조회 (기사용)")
    class GetRepairCase {

        @Test
        @DisplayName("본인 케이스를 정상 조회한다")
        void 본인_케이스를_조회한다() {
            // Arrange
            RepairCase repairCase = caseWith(1L, 10L, RepairStatus.IN_PROGRESS);
            when(repairCaseRepository.findById(1L)).thenReturn(Optional.of(repairCase));

            // Act
            RepairCase result = service.getRepairCase(1L, 10L);

            // Assert
            assertThat(result).isEqualTo(repairCase);
        }

        @Test
        @DisplayName("존재하지 않는 케이스 조회 시 REPAIR_CASE_NOT_FOUND 예외")
        void 존재하지_않는_케이스_조회시_예외() {
            // Arrange
            when(repairCaseRepository.findById(99L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> service.getRepairCase(99L, 10L))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).errorCode())
                    .isEqualTo(ErrorCode.REPAIR_CASE_NOT_FOUND);
        }

        @Test
        @DisplayName("다른 기사의 케이스 접근 시 REPAIR_CASE_ACCESS_DENIED 예외")
        void 다른_기사_케이스_접근시_예외() {
            // Arrange
            RepairCase repairCase = caseWith(1L, 10L, RepairStatus.IN_PROGRESS);
            when(repairCaseRepository.findById(1L)).thenReturn(Optional.of(repairCase));

            // Act & Assert
            assertThatThrownBy(() -> service.getRepairCase(1L, 99L))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).errorCode())
                    .isEqualTo(ErrorCode.REPAIR_CASE_ACCESS_DENIED);
        }
    }

    // ── addDetail ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("수리 내역 추가")
    class AddDetail {

        @Test
        @DisplayName("진행 중 케이스에 수리 내역을 추가하고 총액을 갱신한다")
        void 수리_내역을_추가한다() {
            // Arrange
            RepairCase repairCase = caseWith(1L, 10L, RepairStatus.IN_PROGRESS);
            RepairCase updated = caseWith(1L, 10L, RepairStatus.IN_PROGRESS);
            when(repairCaseRepository.findById(1L)).thenReturn(Optional.of(repairCase));
            when(repairDetailRepository.sumAmountByRepairCaseId(1L)).thenReturn(BigDecimal.valueOf(30000));
            when(repairCaseRepository.updateTotalAmount(1L, BigDecimal.valueOf(30000))).thenReturn(updated);

            // Act
            RepairCase result = service.addDetail(1L, 10L, "부품 교체", BigDecimal.valueOf(30000));

            // Assert
            verify(repairDetailRepository).save(1L, "부품 교체", BigDecimal.valueOf(30000));
            assertThat(result).isEqualTo(updated);
        }

        @Test
        @DisplayName("진행 중이 아닌 케이스에 내역 추가 시 REPAIR_CASE_INVALID_STATUS 예외")
        void 진행중_아닌_케이스에_내역_추가시_예외() {
            // Arrange
            RepairCase submitted = caseWith(1L, 10L, RepairStatus.SUBMITTED);
            when(repairCaseRepository.findById(1L)).thenReturn(Optional.of(submitted));

            // Act & Assert
            assertThatThrownBy(() -> service.addDetail(1L, 10L, "부품 교체", BigDecimal.valueOf(30000)))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).errorCode())
                    .isEqualTo(ErrorCode.REPAIR_CASE_INVALID_STATUS);
            verify(repairDetailRepository, never()).save(any(), any(), any());
        }
    }

    // ── deleteDetail ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("수리 내역 삭제")
    class DeleteDetail {

        @Test
        @DisplayName("수리 내역을 삭제하고 총액을 갱신한다")
        void 수리_내역을_삭제한다() {
            // Arrange
            RepairCase repairCase = caseWith(1L, 10L, RepairStatus.IN_PROGRESS);
            RepairDetail repairDetail = detail(5L, 1L);
            when(repairCaseRepository.findById(1L)).thenReturn(Optional.of(repairCase));
            when(repairDetailRepository.findById(5L)).thenReturn(Optional.of(repairDetail));
            when(repairDetailRepository.sumAmountByRepairCaseId(1L)).thenReturn(BigDecimal.ZERO);

            // Act
            service.deleteDetail(1L, 5L, 10L);

            // Assert
            verify(repairDetailRepository).deleteById(5L);
            verify(repairCaseRepository).updateTotalAmount(1L, BigDecimal.ZERO);
        }

        @Test
        @DisplayName("존재하지 않는 내역 삭제 시 REPAIR_DETAIL_NOT_FOUND 예외")
        void 존재하지_않는_내역_삭제시_예외() {
            // Arrange
            RepairCase repairCase = caseWith(1L, 10L, RepairStatus.IN_PROGRESS);
            when(repairCaseRepository.findById(1L)).thenReturn(Optional.of(repairCase));
            when(repairDetailRepository.findById(99L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> service.deleteDetail(1L, 99L, 10L))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).errorCode())
                    .isEqualTo(ErrorCode.REPAIR_DETAIL_NOT_FOUND);
        }

        @Test
        @DisplayName("다른 케이스의 내역 삭제 시 REPAIR_DETAIL_NOT_IN_CASE 예외")
        void 다른_케이스의_내역_삭제시_예외() {
            // Arrange
            RepairCase repairCase = caseWith(1L, 10L, RepairStatus.IN_PROGRESS);
            RepairDetail detailOfOtherCase = detail(5L, 999L); // 다른 케이스 소속
            when(repairCaseRepository.findById(1L)).thenReturn(Optional.of(repairCase));
            when(repairDetailRepository.findById(5L)).thenReturn(Optional.of(detailOfOtherCase));

            // Act & Assert
            assertThatThrownBy(() -> service.deleteDetail(1L, 5L, 10L))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).errorCode())
                    .isEqualTo(ErrorCode.REPAIR_DETAIL_NOT_IN_CASE);
            verify(repairDetailRepository, never()).deleteById(anyLong());
        }
    }

    // ── submit ────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("완료 제출")
    class Submit {

        @Test
        @DisplayName("진행 중 케이스를 제출 상태로 변경한다")
        void 케이스를_제출한다() {
            // Arrange
            RepairCase repairCase = caseWith(1L, 10L, RepairStatus.IN_PROGRESS);
            RepairCase submitted = caseWith(1L, 10L, RepairStatus.SUBMITTED);
            when(repairCaseRepository.findById(1L)).thenReturn(Optional.of(repairCase));
            when(repairCaseRepository.updateStatus(1L, RepairStatus.SUBMITTED)).thenReturn(submitted);

            // Act
            RepairCase result = service.submit(1L, 10L);

            // Assert
            assertThat(result.status()).isEqualTo(RepairStatus.SUBMITTED);
        }

        @Test
        @DisplayName("진행 중이 아닌 케이스 제출 시 REPAIR_CASE_INVALID_STATUS 예외")
        void 진행중_아닌_케이스_제출시_예외() {
            // Arrange
            RepairCase submitted = caseWith(1L, 10L, RepairStatus.SUBMITTED);
            when(repairCaseRepository.findById(1L)).thenReturn(Optional.of(submitted));

            // Act & Assert
            assertThatThrownBy(() -> service.submit(1L, 10L))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).errorCode())
                    .isEqualTo(ErrorCode.REPAIR_CASE_INVALID_STATUS);
        }
    }

    // ── getAllRepairCases ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("수리 케이스 전체 조회 (관리자용)")
    class GetAllRepairCases {

        @Test
        @DisplayName("상태 없이 조회하면 전체 목록을 반환한다")
        void 상태_없이_전체_조회한다() {
            // Arrange
            List<RepairCase> all = List.of(caseWith(1L, 10L, RepairStatus.IN_PROGRESS));
            when(repairCaseRepository.findAll()).thenReturn(all);

            // Act
            List<RepairCase> result = service.getAllRepairCases(null);

            // Assert
            assertThat(result).isEqualTo(all);
            verify(repairCaseRepository, never()).findAllByStatus(any());
        }

        @Test
        @DisplayName("상태 필터로 조회하면 해당 상태 목록만 반환한다")
        void 상태_필터로_조회한다() {
            // Arrange
            List<RepairCase> submitted = List.of(caseWith(1L, 10L, RepairStatus.SUBMITTED));
            when(repairCaseRepository.findAllByStatus(RepairStatus.SUBMITTED)).thenReturn(submitted);

            // Act
            List<RepairCase> result = service.getAllRepairCases(RepairStatus.SUBMITTED);

            // Assert
            assertThat(result).isEqualTo(submitted);
            verify(repairCaseRepository, never()).findAll();
        }
    }

    // ── approve ───────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("완료 승인 (관리자용)")
    class Approve {

        @Test
        @DisplayName("제출된 케이스를 완료 상태로 승인한다")
        void 케이스를_승인한다() {
            // Arrange
            RepairCase submitted = caseWith(1L, 10L, RepairStatus.SUBMITTED);
            RepairCase completed = caseWith(1L, 10L, RepairStatus.COMPLETED);
            when(repairCaseRepository.findById(1L)).thenReturn(Optional.of(submitted));
            when(repairCaseRepository.updateStatus(1L, RepairStatus.COMPLETED)).thenReturn(completed);

            // Act
            RepairCase result = service.approve(1L);

            // Assert
            assertThat(result.status()).isEqualTo(RepairStatus.COMPLETED);
        }

        @Test
        @DisplayName("제출 상태가 아닌 케이스 승인 시 REPAIR_CASE_INVALID_STATUS 예외")
        void 제출_아닌_케이스_승인시_예외() {
            // Arrange
            RepairCase inProgress = caseWith(1L, 10L, RepairStatus.IN_PROGRESS);
            when(repairCaseRepository.findById(1L)).thenReturn(Optional.of(inProgress));

            // Act & Assert
            assertThatThrownBy(() -> service.approve(1L))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).errorCode())
                    .isEqualTo(ErrorCode.REPAIR_CASE_INVALID_STATUS);
        }
    }

    // ── reject ────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("완료 반려 (관리자용)")
    class Reject {

        @Test
        @DisplayName("제출된 케이스를 사유와 함께 반려하고 진행 중으로 되돌린다")
        void 케이스를_반려한다() {
            // Arrange
            RepairCase submitted = caseWith(1L, 10L, RepairStatus.SUBMITTED);
            RepairCase rejected = caseWith(1L, 10L, RepairStatus.IN_PROGRESS);
            when(repairCaseRepository.findById(1L)).thenReturn(Optional.of(submitted));
            when(repairCaseRepository.updateStatusWithReason(1L, RepairStatus.IN_PROGRESS, "부품 누락"))
                    .thenReturn(rejected);

            // Act
            RepairCase result = service.reject(1L, "부품 누락");

            // Assert
            assertThat(result.status()).isEqualTo(RepairStatus.IN_PROGRESS);
            verify(repairCaseRepository).updateStatusWithReason(1L, RepairStatus.IN_PROGRESS, "부품 누락");
        }

        @Test
        @DisplayName("제출 상태가 아닌 케이스 반려 시 REPAIR_CASE_INVALID_STATUS 예외")
        void 제출_아닌_케이스_반려시_예외() {
            // Arrange
            RepairCase completed = caseWith(1L, 10L, RepairStatus.COMPLETED);
            when(repairCaseRepository.findById(1L)).thenReturn(Optional.of(completed));

            // Act & Assert
            assertThatThrownBy(() -> service.reject(1L, "사유"))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).errorCode())
                    .isEqualTo(ErrorCode.REPAIR_CASE_INVALID_STATUS);
        }
    }
}
