ALTER TABLE mentor_profiles
    ADD COLUMN review_count INTEGER NOT NULL DEFAULT 0;

CREATE TABLE mentor_reviews (
    id BIGSERIAL PRIMARY KEY,
    session_id BIGINT NOT NULL UNIQUE,
    mentor_id BIGINT NOT NULL,
    client_id BIGINT NOT NULL,
    rating SMALLINT NOT NULL,
    comment VARCHAR(2000),
    status VARCHAR(20) NOT NULL,
    moderation_note VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_mentor_reviews_session
        FOREIGN KEY (session_id) REFERENCES sessions(id) ON DELETE CASCADE,
    CONSTRAINT fk_mentor_reviews_mentor
        FOREIGN KEY (mentor_id) REFERENCES mentor_profiles(id) ON DELETE CASCADE,
    CONSTRAINT fk_mentor_reviews_client
        FOREIGN KEY (client_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_mentor_reviews_rating CHECK (rating BETWEEN 1 AND 5),
    CONSTRAINT chk_mentor_reviews_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED'))
);

CREATE INDEX idx_mentor_reviews_public
    ON mentor_reviews (mentor_id, status, created_at DESC);

CREATE INDEX idx_mentor_reviews_moderation
    ON mentor_reviews (status, created_at ASC);
