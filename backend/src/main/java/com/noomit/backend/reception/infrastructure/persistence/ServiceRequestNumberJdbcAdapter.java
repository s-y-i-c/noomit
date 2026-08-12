package com.noomit.backend.reception.infrastructure.persistence;

import java.time.LocalDate;
import com.noomit.backend.reception.application.ServiceRequestNumberRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
class ServiceRequestNumberJdbcAdapter implements ServiceRequestNumberRepository {
    private static final String INCREMENT_AND_GET_SQL = """
            INSERT INTO service_request_number_counter (request_date, last_seq)
            VALUES (?, 1)
            ON CONFLICT (request_date)
            DO UPDATE SET last_seq = service_request_number_counter.last_seq + 1
            RETURNING last_seq
            """;

    private final JdbcTemplate requestNumberJdbcTemplate;

    ServiceRequestNumberJdbcAdapter(@Qualifier("requestNumberJdbcTemplate") JdbcTemplate requestNumberJdbcTemplate) {
        this.requestNumberJdbcTemplate = requestNumberJdbcTemplate;
    }

    @Override
    public int issueSequence(LocalDate requestDate) {
        return requestNumberJdbcTemplate.queryForObject(INCREMENT_AND_GET_SQL, Integer.class, requestDate);
    }
}