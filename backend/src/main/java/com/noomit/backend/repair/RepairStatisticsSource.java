package com.noomit.backend.repair;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

/**
 * 수리 도메인이 통계 도메인에 공개하는 읽기 전용 조회 계약입니다.
 *
 * <p>구현 담당자는 수리 엔티티나 Repository를 외부에 노출하지 않고,
 * 조회 결과를 아래 {@link RepairRecord}로 변환해서 반환해 주세요.</p>
 *
 * <p>구현체는 수리 도메인의 infrastructure 계층에 두고 Spring Bean으로 등록해 주세요.
 * 조회 결과가 없거나 요청 ID가 비어 있을 때는 {@code null}이 아니라 빈 목록을 반환해야 합니다.</p>
 */
public interface RepairStatisticsSource {

    /**
     * 접수 ID들에 연결된 수리 건을 한 번의 배치 조회로 반환합니다.
     *
     * <p>N+1 조회를 피하도록 {@code IN} 조건 등의 단일 배치 쿼리로 구현해 주세요.
     * 존재하지 않는 접수 ID는 결과 목록에서 제외합니다.</p>
     */
    List<RepairRecord> findByServiceRequestIds(Collection<Long> serviceRequestIds);

    /**
     * 통계에 필요한 최소 수리 데이터입니다.
     *
     * @param serviceRequestId repair_cases.service_request_id
     * @param status repair_cases.status
     * @param totalAmount repair_cases.total_amount. DB numeric 타입이므로 {@link BigDecimal}을 사용합니다.
     */
    record RepairRecord(
            long serviceRequestId,
            RepairState status,
            BigDecimal totalAmount) {}

    /** repair_cases.status가 가질 수 있는 수리 상태입니다. */
    enum RepairState {
        IN_PROGRESS,
        SUBMITTED,
        COMPLETED
    }
}
