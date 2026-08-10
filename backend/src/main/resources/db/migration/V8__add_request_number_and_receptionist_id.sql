ALTER TABLE service_request
    ADD COLUMN request_number VARCHAR(30),
    ADD COLUMN receptionist_id BIGINT;

ALTER TABLE service_request
    ALTER COLUMN request_number SET NOT NULL,
ALTER COLUMN receptionist_id SET NOT NULL,
    ADD CONSTRAINT uq_service_request_request_number
        UNIQUE (request_number);

CREATE TABLE service_request_number_counter (
    request_date DATE PRIMARY KEY,
    last_seq INT NOT NULL
);