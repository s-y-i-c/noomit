package com.noomit.backend.reception.infrastructure.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import com.noomit.backend.reception.application.PageResult;
import com.noomit.backend.reception.application.ServiceRequestQueryRepository;
import com.noomit.backend.reception.domain.ServiceRequest;
import com.noomit.backend.reception.domain.ServiceRequestStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
class ServiceRequestJpaQueryAdapter implements ServiceRequestQueryRepository {
    private final ServiceRequestJpaRepository requestJpaRepository;

    @Override
    public PageResult<ServiceRequest> search(ServiceRequestStatus status, boolean ascending, int page, int size) {
        Sort sort = Sort.by(
                ascending ? Sort.Direction.ASC : Sort.Direction.DESC,
                "requestedAt"
        );
        Page<ServiceRequestEntity> result = requestJpaRepository.search(status, PageRequest.of(page, size, sort));
        List<ServiceRequest> content = result.getContent()
                .stream()
                .map(ServiceRequestEntity::toDomain)
                .toList();

        return new PageResult<>(content, page, size, result.getTotalElements());
    }

    @Override
    public Optional<ServiceRequest> findById(long id) {
        return requestJpaRepository.findById(id).map(ServiceRequestEntity::toDomain);
    }

    @Override
    public List<ServiceRequest> findAssignedByTechnicianAndDate(long technicianId, LocalDate date) {
        return requestJpaRepository.findAssignedByTechnicianAndDate(technicianId, date).stream()
                .map(ServiceRequestEntity::toDomain)
                .toList();
    }
}