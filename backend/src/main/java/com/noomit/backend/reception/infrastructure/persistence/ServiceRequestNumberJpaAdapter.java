package com.noomit.backend.reception.infrastructure.persistence;

import java.time.LocalDate;
import com.noomit.backend.reception.application.ServiceRequestNumberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
class ServiceRequestNumberJpaAdapter implements ServiceRequestNumberRepository {
    private final ServiceRequestNumberCounterJpaRepository requestNumberCounterJpaRepository;

    @Override
    public int issueSequence(LocalDate requestDate) {
        return requestNumberCounterJpaRepository.incrementAndGet(requestDate);
    }
}