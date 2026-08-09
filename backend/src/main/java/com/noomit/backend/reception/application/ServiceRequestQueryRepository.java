package com.noomit.backend.reception.application;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import com.noomit.backend.reception.domain.ServiceRequest;
import com.noomit.backend.reception.domain.ServiceRequestStatus;

public interface ServiceRequestQueryRepository {
    PageResult<ServiceRequest> search(ServiceRequestStatus status, boolean ascending, int page, int size);

    Optional<ServiceRequest> findById(long id);
    
    List<ServiceRequest> findAssignedByTechnicianAndDate(long technicianId, LocalDate date);
}