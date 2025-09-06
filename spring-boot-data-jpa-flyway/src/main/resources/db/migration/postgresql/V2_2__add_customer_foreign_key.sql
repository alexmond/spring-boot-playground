ALTER TABLE roles
    ADD COLUMN customer_id BIGINT;

ALTER TABLE roles
    ADD CONSTRAINT fk_roles_customer
        FOREIGN KEY (customer_id)
            REFERENCES customer (id);