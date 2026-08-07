package com.noomit.backend.repair.infrastructure.persistence;

import com.noomit.backend.repair.domain.RepairStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Getter
@Table(name = "repair_cases")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class RepairCaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "service_request_id", nullable = false, unique = true)
    private Long serviceRequestId;

    @Column(name = "technician_id", nullable = false)
    private Long technicianId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RepairStatus status;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "reject_reason")
    private String rejectReason;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    RepairCaseEntity(Long serviceRequestId, Long technicianId) {
        this.serviceRequestId = serviceRequestId;
        this.technicianId = technicianId;
        this.status = RepairStatus.IN_PROGRESS;
        this.totalAmount = BigDecimal.ZERO;
    }

    void updateStatus(RepairStatus status, String rejectReason) {
        this.status = status;
        this.rejectReason = rejectReason;
    }

    void updateTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
}
