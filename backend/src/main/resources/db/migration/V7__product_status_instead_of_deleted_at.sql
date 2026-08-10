-- V7__product_status_instead_of_deleted_at.sql
-- product도 customer(V6)와 동일하게 "삭제"가 아니라 "판매중/단종" 상태로 관리한다.
-- 단, product는 reception 등 다른 모듈이 과거 이력 조회용으로 계속 참조할 수 있어야 하므로
-- deleted_at처럼 조회 자체를 막지 않고, 목록·상세 화면에서만 status로 필터링한다.

ALTER TABLE product
    ADD COLUMN status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE'
        CHECK (status IN ('ACTIVE', 'INACTIVE'));

UPDATE product
SET status = CASE WHEN deleted_at IS NULL THEN 'ACTIVE' ELSE 'INACTIVE' END;

ALTER TABLE product DROP COLUMN deleted_at;
