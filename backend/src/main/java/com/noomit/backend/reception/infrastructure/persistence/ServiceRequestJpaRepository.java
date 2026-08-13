package com.noomit.backend.reception.infrastructure.persistence;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import com.noomit.backend.reception.application.ServiceRequestListRow;
import com.noomit.backend.reception.domain.ServiceRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface ServiceRequestJpaRepository extends JpaRepository<ServiceRequestEntity, Long> {

    @Query("""
            SELECT new com.noomit.backend.reception.application.ServiceRequestListRow(
                e.id, e.requestNumber, e.customerId, e.productId, e.selectedSubCategoryId,
                e.selectedModelName, e.receptionistId, e.technicianId, e.symptom, e.status,
                e.visitDate, e.visitStartTime, e.visitEndTime, e.requestedAt)
            FROM ServiceRequestEntity e
            WHERE (:status IS NULL OR e.status = :status)
            """)
    Page<ServiceRequestListRow> search(@Param("status") ServiceRequestStatus status, Pageable pageable);

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

    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE ServiceRequestEntity e
            SET e.customerId = :customerId,
                e.productId = :productId,
                e.selectedSubCategoryId = :selectedSubCategoryId,
                e.selectedModelName = :selectedModelName,
                e.symptom = :symptom,
                e.remarks = :remarks,
                e.version = e.version + 1
            WHERE e.id = :id
                AND e.status IN (
                    com.noomit.backend.reception.domain.ServiceRequestStatus.RECEIVED,
                    com.noomit.backend.reception.domain.ServiceRequestStatus.ASSIGNED
                )
                AND e.version = :version
            """)
    int update(@Param("id") long id,
               @Param("customerId") long customerId,
               @Param("productId") Long productId,
               @Param("selectedSubCategoryId") Long selectedSubCategoryId,
               @Param("selectedModelName") String selectedModelName,
               @Param("symptom") String symptom,
               @Param("remarks") String remarks,
               @Param("version") long version);

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

    @Query("""
            SELECT e FROM ServiceRequestEntity e
            WHERE e.requestedAt >= :fromInclusive 
                AND e.requestedAt < :toExclusive
                AND (:technicianId IS NULL OR e.technicianId = :technicianId)
                AND (:customerId IS NULL OR e.customerId = :customerId)
                AND (:productId IS NULL OR e.productId = :productId)
            """)
    List<ServiceRequestEntity> findForStatistics(
            @Param("fromInclusive") Instant fromInclusive,
            @Param("toExclusive") Instant toExclusive,
            @Param("technicianId") Long technicianId,
            @Param("customerId") Long customerId,
            @Param("productId") Long productId);
}