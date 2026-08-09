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
            SET e.status = CASE
                WHEN e.status = com.noomit.backend.reception.domain.TechnicianAvailabilityStatus.AVAILABLE
                THEN com.noomit.backend.reception.domain.TechnicianAvailabilityStatus.UNAVAILABLE
                ELSE com.noomit.backend.reception.domain.TechnicianAvailabilityStatus.AVAILABLE
            END
            WHERE e.id = :id AND e.technicianId = :technicianId
            """)
    int toggleAvailability(@Param("id") long id, @Param("technicianId") long technicianId);
}