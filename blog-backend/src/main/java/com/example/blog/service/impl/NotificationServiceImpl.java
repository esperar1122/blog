package com.example.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blog.entity.Notification;
import com.example.blog.exception.BusinessException;
import com.example.blog.mapper.NotificationMapper;
import com.example.blog.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;

    @Override
    @Transactional
    public Notification createNotification(Notification notification) {
        notification.setCreateTime(LocalDateTime.now());
        notification.setIsRead(false);
        notificationMapper.insert(notification);
        return notification;
    }

    @Override
    @Transactional
    public Notification createCommentNotification(Long userId, String title, String content, Long articleId) {
        Notification notification = Notification.createCommentNotification(userId, title, content, articleId);
        return createNotification(notification);
    }

    @Override
    @Transactional
    public Notification createLikeNotification(Long userId, String title, String content, Long articleId) {
        Notification notification = Notification.createLikeNotification(userId, title, content, articleId);
        return createNotification(notification);
    }

    @Override
    @Transactional
    public Notification createReplyNotification(Long userId, String title, String content, Long commentId) {
        Notification notification = Notification.createReplyNotification(userId, title, content, commentId);
        return createNotification(notification);
    }

    @Override
    @Transactional
    public Notification createSystemNotification(Long userId, String title, String content) {
        Notification notification = Notification.createSystemNotification(userId, title, content);
        return createNotification(notification);
    }

    @Override
    public Notification getNotificationById(Long id, Long userId) {
        Notification notification = notificationMapper.selectById(id);
        if (notification == null) {
            throw new BusinessException("通知不存在");
        }
        if (!notification.getUserId().equals(userId)) {
            throw new BusinessException("无权限查看此通知");
        }
        return notification;
    }

    @Override
    public IPage<Notification> getNotificationsWithPagination(Page<Notification> page, Long userId) {
        return notificationMapper.selectNotificationsWithDetails(page, userId);
    }

    @Override
    public List<Notification> getUnreadNotifications(Long userId, Integer limit) {
        return notificationMapper.selectUnreadNotifications(userId, limit);
    }

    @Override
    public int countUnreadNotifications(Long userId) {
        return notificationMapper.countUnreadNotifications(userId);
    }

    @Override
    @Transactional
    public boolean markAsRead(Long id, Long userId) {
        getNotificationById(id, userId);
        return notificationMapper.markAsRead(id) > 0;
    }

    @Override
    @Transactional
    public boolean markAllAsRead(Long userId) {
        return notificationMapper.markAllAsRead(userId) > 0;
    }

    @Override
    @Transactional
    public boolean deleteNotification(Long id, Long userId) {
        getNotificationById(id, userId);
        return notificationMapper.deleteById(id) > 0;
    }

    @Override
    @Transactional
    public boolean deleteAllNotifications(Long userId) {
        QueryWrapper<Notification> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        return notificationMapper.delete(queryWrapper) > 0;
    }

    @Override
    public List<Notification> getNotificationsByType(Long userId, String type) {
        return notificationMapper.selectNotificationsByType(userId, type);
    }

    @Override
    public List<Notification> getRecentNotifications(Long userId, Integer limit) {
        return notificationMapper.selectRecentNotifications(userId, limit);
    }

    @Override
    @Transactional
    public boolean batchDeleteNotifications(List<Long> ids, Long userId) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }

        QueryWrapper<Notification> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                   .in("id", ids);

        return notificationMapper.delete(queryWrapper) > 0;
    }
}