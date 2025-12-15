-- Create initial database schema for the blog system

-- User table
CREATE TABLE t_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(20) NOT NULL UNIQUE COMMENT 'Username',
    email VARCHAR(100) NOT NULL UNIQUE COMMENT 'Email',
    password VARCHAR(100) NOT NULL COMMENT 'Encrypted password',
    nickname VARCHAR(50) COMMENT 'Display name',
    avatar VARCHAR(255) COMMENT 'Avatar URL',
    bio TEXT COMMENT 'User biography',
    role ENUM('USER', 'ADMIN') NOT NULL DEFAULT 'USER' COMMENT 'User role',
    status ENUM('ACTIVE', 'INACTIVE', 'BANNED') NOT NULL DEFAULT 'ACTIVE' COMMENT 'Account status',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    last_login_time DATETIME COMMENT 'Last login time',
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User table';

-- Category table
CREATE TABLE t_category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE COMMENT 'Category name',
    description VARCHAR(200) COMMENT 'Category description',
    icon VARCHAR(50) COMMENT 'Category icon',
    parent_id BIGINT COMMENT 'Parent category ID',
    sort_order INT NOT NULL DEFAULT 0 COMMENT 'Display order',
    article_count INT NOT NULL DEFAULT 0 COMMENT 'Number of articles',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    FOREIGN KEY (parent_id) REFERENCES t_category(id) ON DELETE SET NULL,
    INDEX idx_parent_id (parent_id),
    INDEX idx_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Category table';

-- Tag table
CREATE TABLE t_tag (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(30) NOT NULL UNIQUE COMMENT 'Tag name',
    color VARCHAR(7) NOT NULL DEFAULT '#1890ff' COMMENT 'Tag color in hex',
    article_count INT NOT NULL DEFAULT 0 COMMENT 'Number of articles',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Tag table';

-- Article table
CREATE TABLE t_article (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL COMMENT 'Article title',
    content LONGTEXT NOT NULL COMMENT 'Article content in Markdown',
    summary VARCHAR(500) COMMENT 'Article summary',
    cover_image VARCHAR(255) COMMENT 'Cover image URL',
    status ENUM('DRAFT', 'PUBLISHED', 'DELETED') NOT NULL DEFAULT 'DRAFT' COMMENT 'Article status',
    view_count INT NOT NULL DEFAULT 0 COMMENT 'Number of views',
    like_count INT NOT NULL DEFAULT 0 COMMENT 'Number of likes',
    comment_count INT NOT NULL DEFAULT 0 COMMENT 'Number of comments',
    is_top BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Whether article is pinned',
    author_id BIGINT NOT NULL COMMENT 'Author ID',
    category_id BIGINT COMMENT 'Category ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    publish_time DATETIME COMMENT 'Publication time',
    FOREIGN KEY (author_id) REFERENCES t_user(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES t_category(id) ON DELETE SET NULL,
    INDEX idx_author_id (author_id),
    INDEX idx_category_id (category_id),
    INDEX idx_status (status),
    INDEX idx_publish_time (publish_time),
    INDEX idx_view_count (view_count),
    INDEX idx_is_top (is_top),
    FULLTEXT INDEX ft_title_content (title, content)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Article table';

-- Article Tag junction table
CREATE TABLE t_article_tag (
    article_id BIGINT NOT NULL COMMENT 'Article ID',
    tag_id BIGINT NOT NULL COMMENT 'Tag ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    PRIMARY KEY (article_id, tag_id),
    FOREIGN KEY (article_id) REFERENCES t_article(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES t_tag(id) ON DELETE CASCADE,
    INDEX idx_article_id (article_id),
    INDEX idx_tag_id (tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Article Tag junction table';

-- Comment table
CREATE TABLE t_comment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    content TEXT NOT NULL COMMENT 'Comment content',
    article_id BIGINT NOT NULL COMMENT 'Article ID',
    user_id BIGINT NOT NULL COMMENT 'User ID',
    parent_id BIGINT COMMENT 'Parent comment ID',
    level INT NOT NULL DEFAULT 1 COMMENT 'Comment nesting level',
    like_count INT NOT NULL DEFAULT 0 COMMENT 'Number of likes',
    status ENUM('NORMAL', 'DELETED') NOT NULL DEFAULT 'NORMAL' COMMENT 'Comment status',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    FOREIGN KEY (article_id) REFERENCES t_article(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES t_user(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_id) REFERENCES t_comment(id) ON DELETE CASCADE,
    INDEX idx_article_id (article_id),
    INDEX idx_user_id (user_id),
    INDEX idx_parent_id (parent_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Comment table';

-- Article Like table
CREATE TABLE t_article_like (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    article_id BIGINT NOT NULL COMMENT 'Article ID',
    user_id BIGINT NOT NULL COMMENT 'User ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    UNIQUE KEY uk_article_user (article_id, user_id),
    FOREIGN KEY (article_id) REFERENCES t_article(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES t_user(id) ON DELETE CASCADE,
    INDEX idx_article_id (article_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Article Like table';

-- Notification table
CREATE TABLE t_notification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT 'Recipient user ID',
    type VARCHAR(50) NOT NULL COMMENT 'Notification type',
    title VARCHAR(200) NOT NULL COMMENT 'Notification title',
    content TEXT COMMENT 'Notification content',
    related_id BIGINT COMMENT 'Related entity ID',
    related_type VARCHAR(50) COMMENT 'Related entity type',
    is_read BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Whether notification is read',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    FOREIGN KEY (user_id) REFERENCES t_user(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_is_read (is_read),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Notification table';

-- Insert initial data
INSERT INTO t_user (username, email, password, nickname, role, status) VALUES
('admin', 'admin@blog.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFDYZt/I5/BFnhkSLsVBDSC', '管理员', 'ADMIN', 'ACTIVE');

INSERT INTO t_category (name, description, icon, sort_order) VALUES
('技术', '技术相关文章', 'tech', 1),
('生活', '生活感悟', 'life', 2),
('读书', '读书笔记', 'book', 3),
('旅行', '旅行记录', 'travel', 4);

INSERT INTO t_tag (name, color) VALUES
('Java', '#f89820'),
('Spring Boot', '#6db33f'),
('Vue.js', '#4fc08d'),
('TypeScript', '#3178c6'),
('MySQL', '#4479a1'),
('Redis', '#dc382d'),
('Docker', '#2496ed'),
('Linux', '#fcc624');