package com.noomit.backend.reception.infrastructure.persistence;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import com.noomit.backend.reception.application.AvailabilitySlot;
import com.noomit.backend.reception.application.TechnicianAvailabilityRepository;
import com.noomit.backend.reception.domain.TechnicianAvailability;
import com.noomit.backend.reception.domain.TechnicianAvailabilityStatus;
import com.noomit.backend.shared.error.BusinessException;
import com.noomit.backend.shared.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
class TechnicianAvailabilityJpaAdapter implements TechnicianAvailabilityRepository {
    // Slot INSERT문 JDBC 배치로 나눠서 실행
    private static final int INSERT_BATCH_SIZE = 500;
    private static final String INSERT_SLOT_SQL = """
            INSERT INTO technician_availability
                (technician_id, available_date, start_time, end_time, status, created_at, updated_at)
            VALUES (?, ?, ?, ?, 'AVAILABLE', now(), now())
            ON CONFLICT (technician_id, available_date, start_time, end_time) DO NOTHING
            """;

    private final TechnicianAvailabilityJpaRepository availabilityJpaRepository;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public TechnicianAvailability create(long technicianId, LocalDate availableDate, LocalTime startTime, LocalTime endTime) {
        TechnicianAvailabilityEntity entity = new TechnicianAvailabilityEntity(technicianId, availableDate, startTime, endTime);
        return availabilityJpaRepository.save(entity).toDomain();
    }

    @Override
    public Optional<TechnicianAvailability> findById(long id) {
        return availabilityJpaRepository.findById(id).map(TechnicianAvailabilityEntity::toDomain);
    }

    @Override
    public void occupySlot(long id) {
        int updated = availabilityJpaRepository.occupySlot(id);
        if (updated == 0) {
            throw new BusinessException(ErrorCode.RECEPTION_INVALID_STATUS, "이미 사용된 슬롯입니다.");
        }
    }

    @Override
    public void releaseSlot(long id) {
        int updated = availabilityJpaRepository.releaseSlot(id);
        if (updated == 0) {
            throw new BusinessException(ErrorCode.RECEPTION_INVALID_STATUS, "이미 사용 가능한 슬롯입니다.");
        }
    }

    @Override
    public int updateAvailabilityStatus(long id, long technicianId, TechnicianAvailabilityStatus status) {
        return availabilityJpaRepository.updateAvailabilityStatus(id, technicianId, status);
    }

    @Override
    public void insertAllIfAbsent(List<AvailabilitySlot> slots) {
        if (slots.isEmpty()) {
            return;
        }

        jdbcTemplate.batchUpdate(INSERT_SLOT_SQL, slots, INSERT_BATCH_SIZE, (ps, slot) -> {
            ps.setLong(1, slot.technicianId());
            ps.setObject(2, slot.date());
            ps.setObject(3, slot.startTime());
            ps.setObject(4, slot.endTime());
        });
    }
}