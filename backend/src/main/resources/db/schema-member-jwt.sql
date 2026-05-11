CREATE TABLE members (
    id BIGSERIAL PRIMARY KEY,
    login_id VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    nickname VARCHAR(50) NOT NULL UNIQUE,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    failed_login_count INT NOT NULL DEFAULT 0,
    last_login_at TIMESTAMP NULL,
    password_changed_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL
);

CREATE TABLE member_audit_logs (
    id BIGSERIAL PRIMARY KEY,
    member_id BIGINT NULL,
    event_type VARCHAR(50) NOT NULL,
    ip_address VARCHAR(100) NULL,
    user_agent TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    member_id BIGINT NOT NULL,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    revoked_at TIMESTAMP NULL,
    CONSTRAINT fk_refresh_tokens_member
        FOREIGN KEY (member_id)
        REFERENCES members(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_refresh_tokens_member_id ON refresh_tokens(member_id);
CREATE INDEX idx_refresh_tokens_token_hash ON refresh_tokens(token_hash);

ALTER TABLE boards ADD COLUMN member_id BIGINT NULL;

ALTER TABLE boards
ADD CONSTRAINT fk_boards_member
FOREIGN KEY (member_id)
REFERENCES members(id);

CREATE TABLE comments (
    id BIGSERIAL PRIMARY KEY,
    board_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    parent_id BIGINT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    CONSTRAINT fk_comments_board
        FOREIGN KEY (board_id)
        REFERENCES boards(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_comments_member
        FOREIGN KEY (member_id)
        REFERENCES members(id),
    CONSTRAINT fk_comments_parent
        FOREIGN KEY (parent_id)
        REFERENCES comments(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_comments_board_id ON comments(board_id);
CREATE INDEX idx_comments_parent_id ON comments(parent_id);
CREATE INDEX idx_comments_board_parent_created
    ON comments(board_id, parent_id, created_at);
