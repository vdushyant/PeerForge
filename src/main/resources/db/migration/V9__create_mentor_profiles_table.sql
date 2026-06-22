CREATE TABLE mentor_profiles
(
    id BIGSERIAL PRIMARY KEY,

    user_id BIGINT NOT NULL UNIQUE,

    about VARCHAR(2000),

    hourly_rate DECIMAL(10,2) NOT NULL,

    approval_status VARCHAR(20) NOT NULL,

    sessions_completed INTEGER NOT NULL DEFAULT 0,

    average_rating DECIMAL(2,1),

    created_at TIMESTAMP NOT NULL,

    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_mentor_profiles_user
        FOREIGN KEY(user_id)
            REFERENCES users(id)
            ON DELETE CASCADE
);