package com.noomit.backend.reception.infrastructure.persistence;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import com.noomit.backend.reception.domain.ServiceRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface ServiceRequestJpaRepository extends JpaRepository<ServiceRequestEntity, Long> {

    @Query("""
            SELECT e FROM ServiceRequestEntity e
            WHERE (:status IS NULL OR e.status = :status)
            """)
    Page<ServiceRequestEntity> search(@Param("status") ServiceRequestStatus status, Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE ServiceRequestEntity e
            SET e.technicianId = :technicianId,
                e.reservedSlotId = :slotId,
                e.visitDate = :visitDate,
                e.visitStartTime = :visitStartTime,
                e.visitEndTime = :visitEndTime,
                e.status = com.noomit.backend.reception.domain.ServiceRequestStatus.ASSIGNED,
                e.assignedAt = :assignedAt
            WHERE e.id = :id
                AND e.status = com.noomit.backend.reception.domain.ServiceRequestStatus.RECEIVED
            """)
    int assignInitial(@Param("id") long id,
               @Param("technicianId") long technicianId,
               @Param("slotId") long slotId,
               @Param("visitDate") LocalDate visitDate,
               @Param("visitStartTime") LocalTime visitStartTime,
               @Param("visitEndTime") LocalTime visitEndTime,
               @Param("assignedAt") Instant assignedAt);

    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE ServiceRequestEntity e
            SET e.technicianId = :technicianId,
                e.reservedSlotId = :slotId,
                e.visitDate = :visitDate,
                e.visitStartTime = :visitStartTime,
                e.visitEndTime = :visitEndTime,
                e.assignedAt = :assignedAt,
                e.version = e.version + 1
            WHERE e.id = :id
                AND e.status = com.noomit.backend.reception.domain.ServiceRequestStatus.ASSIGNED
                AND e.version = :version
            """)
    int reassign(@Param("id") long id,
                 @Param("technicianId") long technicianId,
                 @Param("slotId") long slotId,
                 @Param("visitDate") LocalDate visitDate,
                 @Param("visitStartTime") LocalTime visitStartTime,
                 @Param("visitEndTime") LocalTime visitEndTime,
                 @Param("assignedAt") Instant assignedAt,
                 @Param("version") long version);

    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE ServiceRequestEntity e
            SET e.status = com.noomit.backend.reception.domain.ServiceRequestStatus.CANCELLED,
                e.cancelReason = :reason,
                e.cancelledAt = :cancelledAt,
                e.reservedSlotId = NULL,
                e.technicianId = NULL
            WHERE e.id = :id
                AND e.status IN (
                    com.noomit.backend.reception.domain.ServiceRequestStatus.RECEIVED,
                    com.noomit.backend.reception.domain.ServiceRequestStatus.ASSIGNED
                )
            """)
    int cancel(@Param("id") long id,
               @Param("reason") String reason,
               @Param("cancelledAt") Instant cancelledAt);

    @Query("""
            SELECT e FROM ServiceRequestEntity e
            WHERE e.status = com.noomit.backend.reception.domain.ServiceRequestStatus.ASSIGNED
                AND e.technicianId = :technicianId
                AND e.visitDate = :date
            ORDER BY e.visitStartTime ASC
            """)
    List<ServiceRequestEntity> findAssignedByTechnicianAndDate(@Param("technicianId") long technicianId, @Param("date") LocalDate date);

    @Query("""
            SELECT e FROM ServiceRequestEntity e
            WHERE e.id = :requestId
                AND e.technicianId = :technicianId
                AND e.status = com.noomit.backend.reception.domain.ServiceRequestStatus.ASSIGNED
            """)
    Optional<ServiceRequestEntity> findAssignedByTechnicianAndId(@Param("technicianId") long technicianId, @Param("requestId") long requestId);
}