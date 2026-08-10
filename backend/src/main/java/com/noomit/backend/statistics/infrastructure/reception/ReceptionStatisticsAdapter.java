package com.noomit.backend.statistics.infrastructure.reception;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import com.noomit.backend.reception.ReceptionStatisticsSource;
import com.noomit.backend.reception.ReceptionStatisticsSource.ReceptionCriteria;
import com.noomit.backend.statistics.application.StatisticsQuery;
import com.noomit.backend.statistics.application.port.ReceptionStatisticsReader;
import com.noomit.backend.user.UserDirectory;
import com.noomit.backend.user.UserRef;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/** 접수 공개 계약을 통계 출력 포트로 변환하는 선택적 연결 어댑터입니다. */
@Component
@RequiredArgsConstructor
class ReceptionStatisticsAdapter implements ReceptionStatisticsReader {
    private final ObjectProvider<ReceptionStatisticsSource> sourceProvider;
    private final UserDirectory userDirectory;
    private final Clock clock;

    @Override
    public List<ReceptionSnapshot> read(StatisticsQuery query) {
        ReceptionStatisticsSource source = sourceProvider.getIfUnique();
        if (source == null) return List.of();

        Instant fromInclusive = query.from().atStartOfDay(clock.getZone()).toInstant();
        Instant toExclusive = query.to().plusDays(1).atStartOfDay(clock.getZone()).toInstant();
        List<ReceptionStatisticsSource.ReceptionRecord> records = source.find(new ReceptionCriteria(
                fromInclusive,
                toExclusive,
                query.technicianId(),
                query.customerId(),
                query.productId()));

        Map<Long, String> technicianNames = userDirectory.findActiveByIds(records.stream()
                        .map(ReceptionStatisticsSource.ReceptionRecord::technicianId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList()).stream()
                .collect(Collectors.toMap(UserRef::id, UserRef::name, (left, right) -> left));
        return records.stream().map(item -> new ReceptionSnapshot(
                item.serviceRequestId(),
                item.customerId(),
                item.productId(),
                item.technicianId(),
                item.technicianId() == null ? null : technicianNames.get(item.technicianId()),
                item.requestedAt(),
                ReceptionState.valueOf(item.status().name())))
                .toList();
    }

    @Override
    public boolean connected() {
        return sourceProvider.getIfUnique() != null;
    }
}
