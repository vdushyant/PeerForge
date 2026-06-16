CREATE TABLE password_reset_tokens (
                                       id BIGSERIAL PRIMARY KEY,

                                       user_id BIGINT NOT NULL,

                                       token VARCHAR(255) NOT NULL UNIQUE,

                                       expiry_date TIMESTAMP NOT NULL,

                                       used BOOLEAN NOT NULL DEFAULT FALSE,

                                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                       CONSTRAINT fk_password_reset_user
                                           FOREIGN KEY(user_id)
                                               REFERENCES users(id)
                                               ON DELETE CASCADE
);