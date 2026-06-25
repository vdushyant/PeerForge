CREATE TABLE payments
(

    id BIGSERIAL PRIMARY KEY,

    session_id BIGINT NOT NULL UNIQUE,

    amount DECIMAL(10,2) NOT NULL,

    status VARCHAR(20) NOT NULL,

    provider_order_id VARCHAR(255),

    provider_payment_id VARCHAR(255),

    created_at TIMESTAMP NOT NULL,

    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_payments_session

        FOREIGN KEY (session_id)

            REFERENCES sessions(id)

            ON DELETE CASCADE

);