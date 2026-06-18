CREATE TABLE user_profiles
(
    id BIGSERIAL PRIMARY KEY,

    user_id BIGINT NOT NULL UNIQUE,

    headline VARCHAR(150),

    bio VARCHAR(1000),

    years_of_experience DECIMAL(3,1),

    github_url VARCHAR(255),

    linkedin_url VARCHAR(255),

    created_at TIMESTAMP NOT NULL,

    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_user_profiles_user
        FOREIGN KEY (user_id)
            REFERENCES users(id)
            ON DELETE CASCADE
);