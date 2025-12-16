-- 创建登录尝试记录表
CREATE TABLE IF NOT EXISTS `login_attempts` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `ip_address` VARCHAR(45) NOT NULL COMMENT 'IP地址',
    `attempt_count` INT NOT NULL DEFAULT 1 COMMENT '尝试次数',
    `last_attempt_time` DATETIME NOT NULL COMMENT '最后尝试时间',
    `locked_until` DATETIME NULL COMMENT '锁定到期时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    UNIQUE KEY `uk_username_ip` (`username`, `ip_address`) COMMENT '用户名和IP唯一索引',
    KEY `idx_username` (`username`) COMMENT '用户名索引',
    KEY `idx_ip_address` (`ip_address`) COMMENT 'IP地址索引',
    KEY `idx_locked_until` (`locked_until`) COMMENT '锁定时间索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='登录尝试记录表';