package com.noomit.backend.reception.application;

import com.noomit.backend.reception.domain.ServiceRequest;
import com.noomit.backend.reception.domain.ServiceRequestStatus;

public interface ServiceRequestQueryRepository {
    PageResult<ServiceRequest> search(ServiceRequestStatus status, boolean ascending, int page, int size);
}