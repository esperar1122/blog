package com.example.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blog.entity.CommentBlacklist;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface CommentBlacklistMapper extends BaseMapper<CommentBlacklist> {

    /**
     * Get active blacklist entries (not expired)
     */
    List<CommentBlacklist> selectActiveBlacklist();

    /**
     * Check if user is blacklisted
     */
    Boolean isUserBlacklisted(@Param("userId") Long userId);

    /**
     * Get blacklist entry by user ID
     */
    CommentBlacklist selectByUserId(@Param("userId") Long userId);

    /**
     * Delete expired blacklist entries
     */
    Integer deleteExpiredEntries();

    /**
     * Get blacklist entries created by specific admin
     */
    List<CommentBlacklist> selectByBlacklistedBy(@Param("blacklistedBy") Long blacklistedBy);
}