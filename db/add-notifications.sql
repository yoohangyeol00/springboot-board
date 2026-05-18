CREATE TABLE IF NOT EXISTS notifications (
    id BIGSERIAL PRIMARY KEY,
    receiver_id BIGINT NOT NULL,
    board_id BIGINT NOT NULL,
    comment_id BIGINT NOT NULL,
    message VARCHAR(255) NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notifications_receiver
        FOREIGN KEY (receiver_id)
        REFERENCES members(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_notifications_board
        FOREIGN KEY (board_id)
        REFERENCES boards(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_notifications_comment
        FOREIGN KEY (comment_id)
        REFERENCES comments(id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_notifications_receiver_read_created
    ON notifications(receiver_id, is_read, created_at DESC);
