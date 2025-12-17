-- Add comment moderation features

-- Add is_edited and edited_time columns to t_comment table
ALTER TABLE t_comment ADD COLUMN is_edited BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Whether comment has been edited';
ALTER TABLE t_comment ADD COLUMN edited_time DATETIME COMMENT 'Last edit time';

-- Create comment report table
CREATE TABLE t_comment_report (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    comment_id BIGINT NOT NULL COMMENT 'Reported comment ID',
    reporter_id BIGINT NOT NULL COMMENT 'Reporter user ID',
    reason ENUM('SPAM', 'INAPPROPRIATE', 'OFFENSIVE', 'OTHER') NOT NULL COMMENT 'Report reason',
    description TEXT COMMENT 'Report description',
    status ENUM('PENDING', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'PENDING' COMMENT 'Report status',
    reviewer_id BIGINT COMMENT 'Reviewer user ID',
    review_time DATETIME COMMENT 'Review time',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    FOREIGN KEY (comment_id) REFERENCES t_comment(id) ON DELETE CASCADE,
    FOREIGN KEY (reporter_id) REFERENCES t_user(id) ON DELETE CASCADE,
    FOREIGN KEY (reviewer_id) REFERENCES t_user(id) ON DELETE SET NULL,
    INDEX idx_comment_id (comment_id),
    INDEX idx_reporter_id (reporter_id),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Comment report table';

-- Create comment blacklist table
CREATE TABLE t_comment_blacklist (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE COMMENT 'Blacklisted user ID',
    reason TEXT COMMENT 'Blacklist reason',
    blacklisted_by BIGINT NOT NULL COMMENT 'Admin who added to blacklist',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    expire_time DATETIME COMMENT 'Blacklist expiration time',
    FOREIGN KEY (user_id) REFERENCES t_user(id) ON DELETE CASCADE,
    FOREIGN KEY (blacklisted_by) REFERENCES t_user(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_expire_time (expire_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Comment blacklist table';

-- Create sensitive word table
CREATE TABLE t_sensitive_word (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    word VARCHAR(100) NOT NULL UNIQUE COMMENT 'Sensitive word',
    replacement VARCHAR(100) COMMENT 'Replacement word',
    pattern VARCHAR(500) NOT NULL COMMENT 'Regex pattern',
    type ENUM('FILTER', 'BLOCK', 'WARNING') NOT NULL DEFAULT 'FILTER' COMMENT 'Word type',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    INDEX idx_word (word),
    INDEX idx_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Sensitive word table';

-- Insert some default sensitive words
INSERT INTO t_sensitive_word (word, replacement, pattern, type) VALUES
('测试敏感词', '***', '测试敏感词', 'FILTER'),
('spam', '***', '(?i)spam', 'FILTER'),
('垃圾', '***', '垃圾', 'FILTER'),
('违法', '***', '违法', 'BLOCK');