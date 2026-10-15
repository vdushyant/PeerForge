ALTER TABLE mentor_reviews
    ALTER COLUMN rating TYPE INTEGER
    USING rating::INTEGER;
