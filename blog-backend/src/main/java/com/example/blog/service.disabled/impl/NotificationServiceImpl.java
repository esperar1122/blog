package com.example.blog.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blog.entity.Notification;
import com.example.blog.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.List;

/**
 * 通知服务实现（简化版本）
 */
@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {

    @Override
    public Notification createNotification(Notification notification) {
        log.info("创建通知 - userId: {}, title: {}", notification.getUserId(), notification.getTitle());
        // 简化版本：直接返回空的通知
        return new Notification();
    }

    @Override
    public Notification createCommentNotification(Long userId, String title, String content, Long articleId) {
        log.info("创建评论通知 - userId: {}, articleId: {}", userId, articleId);
        return new Notification();
    }

    @Override
    public Notification createLikeNotification(Long userId, String title, String content, Long articleId) {
        log.info("创建点赞通知 - userId: {}, articleId: {}", userId, articleId);
        return new Notification();
    }

    @Override
    public Notification createReplyNotification(Long userId, String title, String content, Long commentId) {
        log.info("创建回复通知 - userId: {}, commentId: {}", userId, commentId);
        return new Notification();
    }

    @Override
    public Notification createSystemNotification(Long userId, String title, String content) {
        log.info("创建系统通知 - userId: {}, title: {}", userId, title);
        return new Notification();
    }

    @Override
    public Notification getNotificationById(Long id, Long userId) {
        log.info("获取通知 - id: {}, userId: {}", id, userId);
        return new Notification();
    }

    @Override
    public IPage<Notification> getNotificationsWithPagination(Page<Notification> page, Long userId) {
        log.info("分页获取通知 - userId: {}, page: {}", userId, page.getCurrent());
        return new Page<>(page.getCurrent(), page.getSize(), 0);
    }

    @Override
    public List<Notification> getUnreadNotifications(Long userId, Integer limit) {
        log.info("获取未读通知 - userId: {}, limit: {}", userId, limit);
        return new ArrayList<>();
    }

    @Override
    public int countUnreadNotifications(Long userId) {
        log.info("统计未读通知数量 - userId: {}", userId);
        return 0;
    }

    @Override
    public boolean markAsRead(Long id, Long userId) {
        log.info("标记通知为已读 - id: {}, userId: {}", id, userId);
        return true;
    }

    @Override
    public boolean markAllAsRead(Long userId) {
        log.info("标记所有通知为已读 - userId: {}", userId);
        return true;
    }

    @Override
    public boolean deleteNotification(Long id, Long userId) {
        log.info("删除通知 - id: {}, userId: {}", id, userId);
        return true;
    }

    @Override
    public boolean deleteAllNotifications(Long userId) {
        log.info("删除所有通知 - userId: {}", userId);
        return true;
    }

    @Override
    public List<Notification> getNotificationsByType(Long userId, String type) {
        log.info("按类型获取通知 - userId: {}, type: {}", userId, type);
        return new ArrayList<>();
    }

    @Override
    public List<Notification> getRecentNotifications(Long userId, Integer limit) {
        log.info("获取最近通知 - userId: {}, limit: {}", userId, limit);
        return new ArrayList<>();
    }

    @Override
    public boolean batchDeleteNotifications(List<Long> ids, Long userId) {
        log.info("批量删除通知 - userId: {}, count: {}", userId, ids.size());
        return true;
    }

    @Override
    public void sendCommentReplyNotification(Long userId, Long commentId, Long articleId) {
        log.info("发送评论回复通知 - userId: {}, commentId: {}, articleId: {}", userId, commentId, articleId);
    }

    @Override
    public void sendNewCommentNotification(Long userId, Long commentId, Long articleId) {
        log.info("发送新评论通知 - userId: {}, commentId: {}, articleId: {}", userId, commentId, articleId);
    }
}