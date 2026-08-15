-- ============================================
-- Repair 도메인 인덱스 추가
-- ============================================
-- repair_cases.technician_id: /api/repair-cases/my (WHERE technician_id = ?)
--   FK 미지정 컬럼이라 인덱스가 없어 Seq Scan 발생 가능.
-- repair_details.repair_case_id: 상세 조회 시 JOIN/WHERE 조건
--   FK REFERENCES 선언이 있어도 Postgres는 자동 인덱스를 생성하지 않음.

CREATE INDEX idx_repair_cases_technician_id
    ON repair_cases (technician_id);

CREATE INDEX idx_repair_details_repair_case_id
    ON repair_details (repair_case_id);
