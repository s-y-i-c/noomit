-- ============================================
-- User 도메인 초기 스키마 (PostgreSQL 호환)
-- ============================================

CREATE TABLE user_role_codes (
    id   BIGINT PRIMARY KEY,
    code VARCHAR(32) NOT NULL UNIQUE
        CHECK (code IN ('PENDING', 'COUNSELOR', 'ENGINEER', 'ADMIN', 'DEVELOPER'))
);

INSERT INTO user_role_codes (id, code) VALUES
    (1, 'PENDING'),
    (2, 'COUNSELOR'),
    (3, 'ENGINEER'),
    (4, 'ADMIN'),
    (5, 'DEVELOPER');

CREATE TABLE user_accounts (
    id            BIGSERIAL PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    name          VARCHAR(100) NOT NULL DEFAULT '',
    status        VARCHAR(16)  NOT NULL DEFAULT 'ACTIVE'
        CHECK (status IN ('ACTIVE', 'INACTIVE')),
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL REFERENCES user_accounts (id),
    role_id BIGINT NOT NULL REFERENCES user_role_codes (id),
    PRIMARY KEY (user_id, role_id)
);
