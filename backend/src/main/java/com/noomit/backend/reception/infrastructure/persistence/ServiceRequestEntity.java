package com.noomit.backend.reception.infrastructure.persistence;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import com.noomit.backend.reception.domain.ServiceRequest;
import com.noomit.backend.reception.domain.ServiceRequestStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Getter
@Table(name = "service_request")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class ServiceRequestEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "request_number", nullable = false, updatable = false, length = 30)
    private String requestNumber;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "receptionist_id", nullable = false)
    private Long receptionistId;

    @Column(name = "symptom", nullable = false)
    private String symptom;

    @Column(name = "remarks")
    private String remarks;

    @Column(name = "base_fee", nullable = false)
    private Integer baseFee;

    @Column(name = "reserved_slot_id")
    private Long reservedSlotId;

    @Column(name = "technician_id")
    private Long technicianId;

    @Column(name = "visit_date")
    private LocalDate visitDate;

    @Column(name = "visit_start_time")
    private LocalTime visitStartTime;

    @Column(name = "visit_end_time")
    private LocalTime visitEndTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ServiceRequestStatus status;

    @Column(name = "requested_at", nullable = false)
    private Instant requestedAt;

    @Column(name = "assigned_at")
    private Instant assignedAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "cancel_reason", length = 300)
    private String cancelReason;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    ServiceRequestEntity(Long customerId, Long productId, Long receptionistId, String requestNumber, String symptom, String remarks, Integer baseFee, Instant requestedAt) {
        this.customerId = customerId;
        this.productId = productId;
        this.receptionistId = receptionistId;
        this.requestNumber = requestNumber;
        this.symptom = symptom;
        this.remarks = remarks;
        this.baseFee = baseFee;
        this.status = ServiceRequestStatus.RECEIVED;
        this.requestedAt = requestedAt;
    }

    ServiceRequest toDomain() {
        return new ServiceRequest(
                id,
                requestNumber,
                customerId,
                productId,
                receptionistId,
                symptom,
                remarks,
                baseFee,
                reservedSlotId,
                technicianId,
                visitDate,
                visitStartTime,
                visitEndTime,
                status,
                requestedAt,
                assignedAt,
                cancelledAt,
                cancelReason,
                version,
                createdAt,
                updatedAt
        );
    }
}