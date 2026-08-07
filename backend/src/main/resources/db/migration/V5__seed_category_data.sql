-- V5__seed_category_data.sql
-- Category 초기 데이터

INSERT INTO category (name) VALUES
    ('에어컨'),
    ('냉장고'),
    ('세탁기/건조기'),
    ('컴퓨터'),
    ('프린터'),
    ('TV'),
    ('모니터'),
    ('청소기'),
    ('공기청정기/제습기'),
    ('주방가전'),
    ('오디오'),
    ('기타');


-- V5__seed_sub_category_data.sql
-- SubCategory 초기 데이터

INSERT INTO sub_category (category_id, name)
SELECT (SELECT category_id FROM category WHERE name = '에어컨'), v.name
FROM (VALUES ('스탠드에어컨'), ('벽걸이에어컨'), ('홈멀티에어컨'), ('시스템에어컨'), ('온풍기')) AS v(name);

INSERT INTO sub_category (category_id, name)
SELECT (SELECT category_id FROM category WHERE name = '냉장고'), v.name
FROM (VALUES ('4도어냉장고'), ('양문형냉장고'), ('일반냉장고'), ('빌트인냉장고'), ('영업용냉장고'), ('와인냉장고'), ('화장품냉장고'), ('김치냉장고')) AS v(name);

INSERT INTO sub_category (category_id, name)
SELECT (SELECT category_id FROM category WHERE name = '세탁기/건조기'), v.name
FROM (VALUES ('드럼세탁기'), ('일반세탁기'), ('의류건조기'), ('에어드레서'), ('슈드레서')) AS v(name);

INSERT INTO sub_category (category_id, name)
SELECT (SELECT category_id FROM category WHERE name = '컴퓨터'), v.name
FROM (VALUES ('노트북/태블릿 PC'), ('데스크톱 PC/올인원')) AS v(name);

INSERT INTO sub_category (category_id, name)
SELECT (SELECT category_id FROM category WHERE name = '프린터'), v.name
FROM (VALUES ('컬러레이저프린터'), ('흑백레이저프린터'), ('고속레이저프린터'), ('팩시밀리'), ('잉크젯프린터')) AS v(name);

INSERT INTO sub_category (category_id, name)
SELECT (SELECT category_id FROM category WHERE name = 'TV'), v.name
FROM (VALUES ('대형TV'), ('프로젝션'), ('일반TV'), ('마이크로TV')) AS v(name);

INSERT INTO sub_category (category_id, name)
SELECT (SELECT category_id FROM category WHERE name = '모니터'), v.name
FROM (VALUES ('일반모니터'), ('대형모니터'), ('빔프로젝터')) AS v(name);

INSERT INTO sub_category (category_id, name)
SELECT (SELECT category_id FROM category WHERE name = '청소기'), v.name
FROM (VALUES ('로봇청소기'), ('일반청소기'), ('무선청소기')) AS v(name);

INSERT INTO sub_category (category_id, name)
SELECT (SELECT category_id FROM category WHERE name = '공기청정기/제습기'), v.name
FROM (VALUES ('공기청정기'), ('제습기'), ('시스템제습기(천장형)')) AS v(name);

INSERT INTO sub_category (category_id, name)
SELECT (SELECT category_id FROM category WHERE name = '주방가전'), v.name
FROM (VALUES ('전자레인지'), ('가스오븐레인지'), ('인덕션'), ('후드'), ('식기세척기'), ('정수기')) AS v(name);

INSERT INTO sub_category (category_id, name)
SELECT (SELECT category_id FROM category WHERE name = '오디오'), v.name
FROM (VALUES ('사운드바'), ('스테레오 오디오'), ('DVD')) AS v(name);

INSERT INTO sub_category (category_id, name)
SELECT (SELECT category_id FROM category WHERE name = '기타'), v.name
FROM (VALUES ('전화기'), ('비데'), ('가습기'), ('선풍기')) AS v(name);

