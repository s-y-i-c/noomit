-- V10__add_customer_name_search_index.sql
-- customer 검색(keyword LIKE '%...%')이 name/phoneNumber 둘 다 leading wildcard로 매칭한다.
-- 일반 B-tree 인덱스는 leading wildcard LIKE를 못 타서 의미가 없고,
-- pg_trgm 기반 GIN 인덱스라야 '%keyword%' 형태의 부분 일치도 가속할 수 있다.

-- name도 phone_number처럼 저장 시점에 공백을 지우는 쪽으로 통일한다(애플리케이션 코드도 함께 변경).
-- 기존에 이미 공백 낀 채로 저장된 행이 있을 수 있어 먼저 백필한다.
UPDATE customer SET name = REPLACE(name, ' ', '') WHERE name LIKE '% %';

CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- name은 이제 항상 공백 없이 저장되므로, 검색 쿼리와 똑같이 LOWER(name)에만 인덱스를 걸면 된다.
CREATE INDEX idx_customer_name_trgm
    ON customer USING gin ((LOWER(name)) gin_trgm_ops);

-- phone_number는 저장 시점에 이미 하이픈 없는 숫자만 남도록 정규화돼서 들어오고
-- 검색 쿼리도 컬럼을 그대로 비교하므로(REPLACE/LOWER 없음), 표현식 없이 컬럼에 바로 건다.
-- 기존 UNIQUE 제약의 B-tree 인덱스는 그대로 두고(정확히 일치하는 findByPhoneNumber용), 이건 별도로 추가한다.
CREATE INDEX idx_customer_phone_number_trgm
    ON customer USING gin (phone_number gin_trgm_ops);
