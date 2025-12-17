-- Add comment like table and related indexes
-- Create comment like table for comment voting functionality

CREATE TABLE t_comment_like (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    comment_id BIGINT NOT NULL COMMENT 'Comment ID',
    user_id BIGINT NOT NULL COMMENT 'User ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    UNIQUE KEY uk_comment_user (comment_id, user_id),
    FOREIGN KEY (comment_id) REFERENCES t_comment(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES t_user(id) ON DELETE CASCADE,
    INDEX idx_comment_id (comment_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Comment Like table';

-- Add indexes for comment table to improve query performance
CREATE INDEX idx_article_create_time ON t_comment(article_id, create_time DESC);
CREATE INDEX idx_like_count ON t_comment(like_count DESC);
CREATE INDEX idx_status_create_time ON t_comment(status, create_time DESC);