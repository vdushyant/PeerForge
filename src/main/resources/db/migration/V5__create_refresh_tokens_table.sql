CREATE TABLE refresh_tokens (
                                id BIGSERIAL PRIMARY KEY,

                                user_id BIGINT NOT NULL,

                                token VARCHAR(500) NOT NULL UNIQUE,

                                expiry_date TIMESTAMP NOT NULL,

                                revoked BOOLEAN NOT NULL DEFAULT FALSE,

                                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                CONSTRAINT fk_refresh_token_user
                                    FOREIGN KEY(user_id)
                                        REFERENCES users(id)
                                        ON DELETE CASCADE
);