CREATE TABLE notifications
(
    id BIGSERIAL PRIMARY KEY,
    recipient_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    notification_type VARCHAR(40) NOT NULL,
    title VARCHAR(120) NOT NULL,
    message VARCHAR(1000) NOT NULL,
    action_url VARCHAR(300),
    read_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_notifications_recipient_created
    ON notifications (recipient_id, created_at DESC);

CREATE INDEX idx_notifications_unread
    ON notifications (recipient_id)
    WHERE read_at IS NULL;
