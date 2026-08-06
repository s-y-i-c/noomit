-- V2__create_customer_product_tables.sql
-- Category, SubCategory, Product, Customer 테이블 생성

-- =========================
-- Customer
-- =========================
CREATE TABLE customer (
      customer_id     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
      name            VARCHAR(30) NOT NULL,
      phone_number    VARCHAR(20) UNIQUE,
      zip_code        VARCHAR(10),
      address         VARCHAR(100),
      detail_address  VARCHAR(100),
      memo            VARCHAR(300),
      deleted_at      TIMESTAMPTZ,
      created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
      updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- =========================
-- Category
-- =========================
CREATE TABLE category (
      category_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
      name        VARCHAR(30) NOT NULL
);

-- =========================
-- SubCategory
-- =========================
CREATE TABLE sub_category (
      sub_category_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
      category_id     BIGINT NOT NULL,
      name            VARCHAR(30) NOT NULL,
      CONSTRAINT fk_sub_category_category
          FOREIGN KEY (category_id)
              REFERENCES category (category_id)
);

-- =========================
-- Product
-- =========================
CREATE TABLE product (
     product_id       BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
     category_id      BIGINT NOT NULL,
     sub_category_id  BIGINT NOT NULL,
     model_name       VARCHAR(100),
     model_code       VARCHAR(50) NOT NULL UNIQUE,
     memo             VARCHAR(300),
     deleted_at       TIMESTAMPTZ,
     created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
     updated_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
     CONSTRAINT fk_product_category
         FOREIGN KEY (category_id)
             REFERENCES category (category_id),
     CONSTRAINT fk_product_sub_category
         FOREIGN KEY (sub_category_id)
             REFERENCES sub_category (sub_category_id)
);