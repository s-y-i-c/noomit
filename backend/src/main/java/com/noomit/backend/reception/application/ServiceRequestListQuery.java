package com.noomit.backend.reception.application;

import com.noomit.backend.reception.domain.ServiceRequestStatus;
import com.noomit.backend.shared.error.BusinessException;
import com.noomit.backend.shared.error.ErrorCode;

public record ServiceRequestListQuery(ServiceRequestStatus status, boolean ascending, int page, int size) {

    public static ServiceRequestListQuery of(String status, String sort, int page, int size) {
        if (page < 0) {
            throw new BusinessException(ErrorCode.RECEPTION_INVALID_REQUEST, "page는 0 이상이어야 합니다.");
        }
        if (size < 1) {
            throw new BusinessException(ErrorCode.RECEPTION_INVALID_REQUEST, "size는 1 이상이어야 합니다.");
        }
        return new ServiceRequestListQuery(parseStatus(status), parseAscending(sort), page, size);
    }

    public static ServiceRequestStatus parseStatus(String status) {
        if (status == null || status.isBlank()) return null;
        try {
            return ServiceRequestStatus.valueOf(status);
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.RECEPTION_INVALID_REQUEST, "허용되지 않은 status 값: " + status);
        }
    }

    public static boolean parseAscending(String sort) {
        String value = sort == null ? "requestedAt,desc" : sort;
        return switch (value) {
            case "requestedAt,desc" -> false;
            case "requestedAt,asc" -> true;
            default -> throw new BusinessException(ErrorCode.RECEPTION_INVALID_REQUEST, "허용되지 않은 정렬 값: " + sort);
        };
    }
}