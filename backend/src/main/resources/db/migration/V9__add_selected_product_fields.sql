ALTER TABLE service_request
    ALTER COLUMN product_id DROP NOT NULL,
    ADD COLUMN selected_sub_category_id BIGINT,
    ADD COLUMN selected_model_name VARCHAR(100);

ALTER TABLE service_request
    ADD CONSTRAINT ck_service_request_product_source CHECK (
        (product_id IS NOT NULL 
             AND selected_sub_category_id IS NULL 
             AND selected_model_name IS NULL
         )
        OR
        (product_id IS NULL 
             AND selected_sub_category_id IS NOT NULL 
             AND selected_model_name IS NOT NULL
         )
    );