package com.noomit.backend.reception.application;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import com.noomit.backend.reception.domain.ServiceRequestStatus;

// 목록 조회 전용 프로젝션. 
// ServiceRequestEntity(23컬럼) 전체를 하이드레이션하지 않고 toListItem()에서 실제로 쓰는 14개 컬럼만 담는다.
public record ServiceRequestListRow(
        Long id,
        String requestNumber,
        Long customerId,
        Long productId,
        Long selectedSubCategoryId,
        String selectedModelName,
        Long receptionistId,
        Long technicianId,
        String symptom,
        ServiceRequestStatus status,
        LocalDate visitDate,
        LocalTime visitStartTime,
        LocalTime visitEndTime,
        Instant requestedAt) {
}