-- 创建管理员操作日志表
CREATE TABLE IF NOT EXISTS t_admin_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    admin_id BIGINT NOT NULL COMMENT '管理员ID',
    admin_username VARCHAR(50) NOT NULL COMMENT '管理员用户名',
    operation VARCHAR(100) NOT NULL COMMENT '操作类型',
    target_user_id BIGINT COMMENT '目标用户ID',
    target_username VARCHAR(50) COMMENT '目标用户名',
    details TEXT COMMENT '操作详情',
    ip_address VARCHAR(45) COMMENT 'IP地址',
    user_agent TEXT COMMENT '用户代理',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_admin_id (admin_id),
    INDEX idx_target_user_id (target_user_id),
    INDEX idx_operation (operation),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员操作日志表';

-- 为用户表添加索引以优化查询性能
ALTER TABLE t_user ADD INDEX IF NOT EXISTS idx_username (username);
ALTER TABLE t_user ADD INDEX IF NOT EXISTS idx_email (email);
ALTER TABLE t_user ADD INDEX IF NOT EXISTS idx_role_status (role, status);
ALTER TABLE t_user ADD INDEX IF NOT EXISTS idx_create_time (create_time);
ALTER TABLE t_user ADD INDEX IF NOT EXISTS idx_is_deleted (is_deleted);