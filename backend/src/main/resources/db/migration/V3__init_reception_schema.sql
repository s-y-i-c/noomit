
-- =========================
-- technician_availability
-- 기사 가능 시간 슬롯
-- =========================

CREATE TABLE technician_availability (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    technician_id BIGINT NOT NULL,
    available_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    status VARCHAR(20) NOT NULL
        CHECK (status IN ('AVAILABLE', 'UNAVAILABLE')),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uq_technician_availability_slot
        UNIQUE (technician_id, available_date, start_time, end_time)
);

-- =========================
-- service_request
-- A/S 접수
-- =========================

CREATE TABLE service_request (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    symptom TEXT NOT NULL,
    remarks TEXT,
    base_fee INTEGER NOT NULL,
    reserved_slot_id BIGINT,
    technician_id BIGINT,

    visit_date DATE,
    visit_start_time TIME,
    visit_end_time TIME,

    status VARCHAR(20) NOT NULL
        CHECK (status IN ('RECEIVED', 'ASSIGNED', 'CANCELLED')),

    requested_at TIMESTAMPTZ NOT NULL,
    assigned_at TIMESTAMPTZ,
    cancelled_at TIMESTAMPTZ,
    cancel_reason VARCHAR(300),

    version BIGINT NOT NULL DEFAULT 0,

    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT fk_service_request_reserved_slot
        FOREIGN KEY (reserved_slot_id)
            REFERENCES technician_availability(id),

    CONSTRAINT ck_service_request_cancelled_at
        CHECK (
            (status = 'CANCELLED' AND cancelled_at IS NOT NULL)
                OR status <> 'CANCELLED'
        )
);


-- 같은 슬롯 중복 배정 방지
CREATE UNIQUE INDEX uq_service_request_reserved_slot
    ON service_request(reserved_slot_id)
    WHERE reserved_slot_id IS NOT NULL;