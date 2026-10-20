ALTER TABLE payments
    ADD COLUMN provider_qr_code_id VARCHAR(255) UNIQUE,
    ADD COLUMN provider_qr_code_image_url VARCHAR(1000);
