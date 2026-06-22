CREATE TABLE mentor_availabilities
(

    id BIGSERIAL PRIMARY KEY,

    mentor_id BIGINT NOT NULL,

    day_of_week VARCHAR(20) NOT NULL,

    start_time TIME NOT NULL,

    end_time TIME NOT NULL,

    created_at TIMESTAMP NOT NULL,

    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT
        fk_mentor_availabilities_mentor

        FOREIGN KEY(mentor_id)

            REFERENCES mentor_profiles(id)

            ON DELETE CASCADE

);