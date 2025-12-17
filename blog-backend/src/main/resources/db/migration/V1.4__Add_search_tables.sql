-- 创建搜索相关表
-- V1.4__Add_search_tables.sql

-- 创建搜索历史表
CREATE TABLE t_search_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    keyword VARCHAR(100) NOT NULL COMMENT '搜索关键词',
    result_count INT DEFAULT 0 COMMENT '搜索结果数量',
    search_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '搜索时间',
    INDEX idx_user_search_time (user_id, search_time),
    INDEX idx_keyword (keyword)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='搜索历史表';

-- 创建搜索统计表
CREATE TABLE t_search_stats (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    keyword VARCHAR(100) NOT NULL UNIQUE COMMENT '搜索关键词',
    search_count INT DEFAULT 1 COMMENT '搜索次数',
    avg_result_count DECIMAL(10,2) DEFAULT 0 COMMENT '平均结果数量',
    last_search_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后搜索时间',
    INDEX idx_search_count (search_count DESC),
    INDEX idx_last_search_time (last_search_time DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='搜索统计表';

-- 创建热门关键词表
CREATE TABLE t_hot_keywords (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    keyword VARCHAR(100) NOT NULL UNIQUE COMMENT '热门关键词',
    search_count INT DEFAULT 0 COMMENT '搜索次数',
    position INT DEFAULT 0 COMMENT '排名位置',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_position (position ASC),
    INDEX idx_search_count (search_count DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='热门关键词表';

-- 为t_article表添加全文索引
ALTER TABLE t_article ADD FULLTEXT INDEX ft_title_content (title, content);

-- 添加外键约束（如果t_user表存在）
ALTER TABLE t_search_history
ADD CONSTRAINT fk_search_history_user_id
FOREIGN KEY (user_id) REFERENCES t_user(id) ON DELETE CASCADE;