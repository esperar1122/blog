package com.example.blog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blog.entity.Notification;

import java.util.List;

public interface NotificationService {

    Notification createNotification(Notification notification);

    Notification createCommentNotification(Long userId, String title, String content, Long articleId);

    Notification createLikeNotification(Long userId, String title, String content, Long articleId);

    Notification createReplyNotification(Long userId, String title, String content, Long commentId);

    Notification createSystemNotification(Long userId, String title, String content);

    Notification getNotificationById(Long id, Long userId);

    IPage<Notification> getNotificationsWithPagination(Page<Notification> page, Long userId);

    List<Notification> getUnreadNotifications(Long userId, Integer limit);

    int countUnreadNotifications(Long userId);

    boolean markAsRead(Long id, Long userId);

    boolean markAllAsRead(Long userId);

    boolean deleteNotification(Long id, Long userId);

    boolean deleteAllNotifications(Long userId);

    List<Notification> getNotificationsByType(Long userId, String type);

    List<Notification> getRecentNotifications(Long userId, Integer limit);

    boolean batchDeleteNotifications(List<Long> ids, Long userId);

    void sendCommentReplyNotification(Long userId, Long commentId, Long articleId);

    void sendNewCommentNotification(Long userId, Long commentId, Long articleId);
}