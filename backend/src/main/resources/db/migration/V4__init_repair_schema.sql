-- ============================================
-- Repair 도메인 초기 스키마
-- ============================================

CREATE TABLE repair_cases (
    id                 BIGSERIAL PRIMARY KEY,
    service_request_id BIGINT       NOT NULL UNIQUE,
    technician_id      BIGINT       NOT NULL,
    status             VARCHAR(20)  NOT NULL DEFAULT 'IN_PROGRESS'
        CHECK (status IN ('IN_PROGRESS', 'SUBMITTED', 'COMPLETED')),
    total_amount       DECIMAL(10, 2) NOT NULL DEFAULT 0,
    reject_reason      VARCHAR(500),
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at         TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE repair_details (
    id             BIGSERIAL PRIMARY KEY,
    repair_case_id BIGINT        NOT NULL REFERENCES repair_cases (id),
    description    VARCHAR(500)  NOT NULL,
    amount         DECIMAL(10, 2) NOT NULL,
    created_at     TIMESTAMPTZ   NOT NULL DEFAULT now()
);
