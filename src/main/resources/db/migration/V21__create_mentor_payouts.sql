CREATE TABLE mentor_payouts (
    id BIGSERIAL PRIMARY KEY,
    payment_id BIGINT NOT NULL UNIQUE REFERENCES payments(id) ON DELETE CASCADE,
    mentor_id BIGINT NOT NULL REFERENCES mentor_profiles(id) ON DELETE CASCADE,
    amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(32) NOT NULL,
    provider_transfer_id VARCHAR(255) UNIQUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

INSERT INTO mentor_payouts (
    payment_id,
    mentor_id,
    amount,
    status,
    created_at,
    updated_at
)
SELECT p.id,
       s.mentor_id,
       p.amount,
       'PENDING_ROUTE_SETUP',
       CURRENT_TIMESTAMP,
       CURRENT_TIMESTAMP
FROM payments p
JOIN sessions s ON s.id = p.session_id
WHERE p.status = 'SUCCESS'
  AND s.status = 'COMPLETED';
