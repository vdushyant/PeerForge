ALTER TABLE payments
    ADD COLUMN mentor_amount DECIMAL(10, 2),
    ADD COLUMN client_fee DECIMAL(10, 2);

UPDATE payments
SET mentor_amount = amount,
    client_fee = 0;

ALTER TABLE payments
    ALTER COLUMN mentor_amount SET NOT NULL,
    ALTER COLUMN client_fee SET NOT NULL;
