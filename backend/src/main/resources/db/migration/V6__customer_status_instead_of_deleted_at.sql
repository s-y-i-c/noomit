-- V6__customer_status_instead_of_deleted_at.sql
-- customer는 전화 유입 시 자동 접수되는 대상이라 "삭제"가 아니라 "활성/비활성 토글"이 실제 요구사항에 맞다.
-- deleted_at 대신 status를 두고, phone_number는 계속 전체 유일성을 유지한다
-- (같은 번호로 다시 접수되면 새 row를 만드는 게 아니라 기존 row를 갱신/재활성화한다).

ALTER TABLE customer
    ADD COLUMN status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE'
        CHECK (status IN ('ACTIVE', 'INACTIVE'));

UPDATE customer
SET status = CASE WHEN deleted_at IS NULL THEN 'ACTIVE' ELSE 'INACTIVE' END;

ALTER TABLE customer DROP COLUMN deleted_at;
