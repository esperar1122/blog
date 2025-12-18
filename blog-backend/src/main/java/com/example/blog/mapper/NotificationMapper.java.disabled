package com.example.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blog.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {

    IPage<Notification> selectNotificationsWithDetails(Page<Notification> page, @Param("userId") Long userId);

    List<Notification> selectUnreadNotifications(@Param("userId") Long userId, @Param("limit") Integer limit);

    int countUnreadNotifications(@Param("userId") Long userId);

    int markAsRead(@Param("id") Long id);

    int markAllAsRead(@Param("userId") Long userId);

    int deleteNotificationsByUserId(@Param("userId") Long userId);

    List<Notification> selectNotificationsByType(@Param("userId") Long userId, @Param("type") String type);

    List<Notification> selectRecentNotifications(@Param("userId") Long userId, @Param("limit") Integer limit);
}