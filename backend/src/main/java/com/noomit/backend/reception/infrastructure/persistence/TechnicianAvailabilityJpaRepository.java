package com.noomit.backend.reception.infrastructure.persistence;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import com.noomit.backend.reception.application.MyAvailabilitySlot;
import com.noomit.backend.reception.domain.TechnicianAvailabilityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface TechnicianAvailabilityJpaRepository extends JpaRepository<TechnicianAvailabilityEntity, Long> {

    // 실제 슬롯이 생성돼 있고 예약 가능한 상태인 날짜만 모아 반환
    @Query("""
            SELECT DISTINCT e.availableDate FROM TechnicianAvailabilityEntity e
            WHERE e.availableDate > :from
                AND e.status = com.noomit.backend.reception.domain.TechnicianAvailabilityStatus.AVAILABLE
            ORDER BY e.availableDate ASC
            """)
    List<LocalDate> findDistinctAvailableDatesFrom(@Param("from") LocalDate from);

    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE TechnicianAvailabilityEntity e
            SET e.status = com.noomit.backend.reception.domain.TechnicianAvailabilityStatus.UNAVAILABLE
            WHERE e.id = :id
                AND e.status = com.noomit.backend.reception.domain.TechnicianAvailabilityStatus.AVAILABLE
            """)
    int occupySlot(@Param("id") long id);

    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE TechnicianAvailabilityEntity e
            SET e.status = com.noomit.backend.reception.domain.TechnicianAvailabilityStatus.AVAILABLE
            WHERE e.id = :id
                AND e.status = com.noomit.backend.reception.domain.TechnicianAvailabilityStatus.UNAVAILABLE
            """)
    int releaseSlot(@Param("id") long id);

    @Query("""
            SELECT e.startTime, e.endTime,
                MAX(CASE WHEN e.status = com.noomit.backend.reception.domain.TechnicianAvailabilityStatus.AVAILABLE THEN 1 ELSE 0 END)
            FROM TechnicianAvailabilityEntity e
            WHERE e.availableDate = :date
            GROUP BY e.startTime, e.endTime
            ORDER BY e.startTime
            """)
    List<Object[]> findByDate(@Param("date") LocalDate date);

    List<TechnicianAvailabilityEntity> findByAvailableDateAndStartTimeAndEndTimeAndStatus(
            LocalDate availableDate, LocalTime startTime, LocalTime endTime, TechnicianAvailabilityStatus status);

    @Query("""
            SELECT NEW com.noomit.backend.reception.application.MyAvailabilitySlot(
                a.id, a.startTime, a.endTime, a.status,
                CASE WHEN EXISTS (
                    SELECT 1 FROM ServiceRequestEntity sr
                    WHERE sr.reservedSlotId = a.id
                ) THEN true ELSE false END
            )
            FROM TechnicianAvailabilityEntity a
            WHERE a.technicianId = :technicianId
                AND a.availableDate = :date
            ORDER BY a.startTime
            """)
    List<MyAvailabilitySlot> findByTechnicianAndDate(@Param("technicianId") long technicianId,
                                                       @Param("date") LocalDate date);
    
    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE TechnicianAvailabilityEntity e
            SET e.status = :status
            WHERE e.id = :id
                AND e.technicianId = :technicianId
                AND NOT EXISTS (
                    SELECT 1 FROM ServiceRequestEntity sr
                    WHERE sr.reservedSlotId = e.id
                )
            """)
    int updateAvailabilityStatus(@Param("id") long id, @Param("technicianId") long technicianId,
                            @Param("status") TechnicianAvailabilityStatus status);

    @Modifying(clearAutomatically = true)
    @Query("""
            DELETE FROM TechnicianAvailabilityEntity e
            WHERE e.technicianId = :technicianId
                AND e.availableDate >= :from
                AND NOT EXISTS (
                    SELECT 1 FROM ServiceRequestEntity sr
                    WHERE sr.reservedSlotId = e.id
                )
            """)
    int deleteUnassignedFutureSlots(@Param("technicianId") long technicianId, @Param("from") LocalDate from);
}