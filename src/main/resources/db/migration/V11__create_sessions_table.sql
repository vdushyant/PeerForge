CREATE TABLE sessions
(

    id BIGSERIAL PRIMARY KEY,

    mentor_id BIGINT NOT NULL,

    client_id BIGINT NOT NULL,

    start_datetime TIMESTAMP NOT NULL,

    end_datetime TIMESTAMP NOT NULL,

    status VARCHAR(20) NOT NULL,

    meeting_link VARCHAR(500),

    created_at TIMESTAMP NOT NULL,

    updated_at TIMESTAMP NOT NULL,


    CONSTRAINT fk_sessions_mentor

        FOREIGN KEY (mentor_id)

            REFERENCES mentor_profiles(id)

            ON DELETE CASCADE,


    CONSTRAINT fk_sessions_client

        FOREIGN KEY (client_id)

            REFERENCES users(id)

            ON DELETE CASCADE

);