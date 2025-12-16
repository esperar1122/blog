-- Add article lifecycle management fields to t_article table
ALTER TABLE `t_article`
ADD COLUMN `deleted_at` datetime NULL COMMENT '软删除时间' AFTER `publish_time`,
ADD COLUMN `scheduled_publish_time` datetime NULL COMMENT '定时发布时间' AFTER `deleted_at`;

-- Create article version history table
CREATE TABLE IF NOT EXISTS `t_article_version` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `article_id` bigint NOT NULL COMMENT '文章ID',
  `version_number` int NOT NULL COMMENT '版本号',
  `title` varchar(200) NOT NULL COMMENT '文章标题',
  `content` longtext COMMENT '文章内容(Markdown格式)',
  `summary` varchar(500) COMMENT '文章摘要',
  `cover_image` varchar(500) COMMENT '封面图片URL',
  `change_reason` varchar(500) COMMENT '变更原因',
  `editor_id` bigint NOT NULL COMMENT '编辑者ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_article_id` (`article_id`),
  KEY `idx_version_number` (`version_number`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `fk_article_version_article_id` FOREIGN KEY (`article_id`) REFERENCES `t_article` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_article_version_editor_id` FOREIGN KEY (`editor_id`) REFERENCES `t_user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章版本历史表';

-- Create article operation log table
CREATE TABLE IF NOT EXISTS `t_article_operation_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `article_id` bigint NOT NULL COMMENT '文章ID',
  `operation_type` varchar(50) NOT NULL COMMENT '操作类型(PUBLISH, UNPUBLISH, PIN, UNPIN, SOFT_DELETE, RESTORE, SCHEDULE_PUBLISH, UPDATE)',
  `old_status` varchar(20) COMMENT '操作前状态',
  `new_status` varchar(20) COMMENT '操作后状态',
  `operator_id` bigint NOT NULL COMMENT '操作者ID',
  `operator_ip` varchar(50) COMMENT '操作者IP地址',
  `operation_detail` text COMMENT '操作详情(JSON格式)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`),
  KEY `idx_article_id` (`article_id`),
  KEY `idx_operation_type` (`operation_type`),
  KEY `idx_operator_id` (`operator_id`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `fk_article_log_article_id` FOREIGN KEY (`article_id`) REFERENCES `t_article` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_article_log_operator_id` FOREIGN KEY (`operator_id`) REFERENCES `t_user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章操作日志表';

-- Add indexes for improved query performance
CREATE INDEX `idx_article_status_scheduled` ON `t_article` (`status`, `scheduled_publish_time`);
CREATE INDEX `idx_article_deleted_at` ON `t_article` (`deleted_at`);