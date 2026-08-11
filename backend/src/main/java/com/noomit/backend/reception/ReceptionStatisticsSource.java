package com.noomit.backend.reception;

import java.time.Instant;
import java.util.List;

/**
 * 접수 도메인이 통계 도메인에 공개하는 읽기 전용 조회 계약입니다.
 *
 * <p>구현 담당자는 접수 엔티티나 Repository를 외부에 노출하지 않고,
 * 조회 결과를 아래 {@link ReceptionRecord}로 변환해서 반환해 주세요.</p>
 *
 * <p>구현체는 접수 도메인의 infrastructure 계층에 두고 Spring Bean으로 등록해 주세요.
 * 조회 결과가 없을 때는 {@code null}이 아니라 빈 목록을 반환해야 합니다.</p>
 */
public interface ReceptionStatisticsSource {

    /**
     * 조건에 해당하는 접수를 한 번의 배치 조회로 반환합니다.
     *
     * <p>기간 조건은 {@code fromInclusive <= requestedAt < toExclusive}입니다.
     * 기사·고객·제품 ID가 {@code null}이면 해당 조건은 적용하지 않습니다.</p>
     */
    List<ReceptionRecord> find(ReceptionCriteria criteria);

    /**
     * 접수 통계 조회 조건입니다.
     *
     * @param fromInclusive 접수시각 조회 시작값(포함)
     * @param toExclusive 접수시각 조회 종료값(미포함)
     * @param technicianId 기사 ID 필터, 전체 조회는 {@code null}
     * @param customerId 고객 ID 필터, 전체 조회는 {@code null}
     * @param productId 제품 ID 필터, 전체 조회는 {@code null}
     */
    record ReceptionCriteria(
            Instant fromInclusive,
            Instant toExclusive,
            Long technicianId,
            Long customerId,
            Long productId) {}

    /**
     * 통계에 필요한 최소 접수 데이터입니다.
     *
     * <p>운영 DB의 service_request 컬럼을 기준으로 하며, 고객명·제품명·기사명 같은
     * 다른 도메인의 정보는 포함하지 않습니다.</p>
     */
    record ReceptionRecord(
            long serviceRequestId,
            long customerId,
            Long productId,
            Long technicianId,
            Instant requestedAt,
            ReceptionState status) {}

    /** service_request.status가 가질 수 있는 접수 상태입니다. */
    enum ReceptionState {
        RECEIVED,
        ASSIGNED,
        CANCELLED
    }
}
