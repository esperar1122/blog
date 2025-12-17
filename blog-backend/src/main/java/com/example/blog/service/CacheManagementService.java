package com.example.blog.service;

import com.example.blog.dto.response.CacheStatsResponse;

import java.util.List;
import java.util.Map;

/**
 * 缓存管理服务接口
 */
public interface CacheManagementService {

    /**
     * 获取缓存统计信息
     * @return 缓存统计
     */
    CacheStatsResponse getCacheStats();

    /**
     * 清理指定模式的缓存键
     * @param pattern 缓存键模式
     * @return 清理的键数量
     */
    Long clearCacheByPattern(String pattern);

    /**
     * 清理所有缓存
     * @return 清理结果
     */
    Map<String, Object> clearAllCache();

    /**
     * 获取缓存键列表
     * @param pattern 模式
     * @param limit 限制数量
     * @return 缓存键列表
     */
    List<String> getCacheKeys(String pattern, Integer limit);

    /**
     * 获取缓存值
     * @param key 缓存键
     * @return 缓存值
     */
    Object getCacheValue(String key);

    /**
     * 设置缓存值
     * @param key 缓存键
     * @param value 缓存值
     * @param ttl 过期时间（秒）
     */
    void setCacheValue(String key, Object value, Long ttl);

    /**
     * 删除缓存键
     * @param key 缓存键
     */
    void deleteCacheKey(String key);

    /**
     * 检查缓存键是否存在
     * @param key 缓存键
     * @return 是否存在
     */
    Boolean hasCacheKey(String key);

    /**
     * 获取缓存键的过期时间
     * @param key 缓存键
     * @return 过期时间（秒），-1表示永不过期
     */
    Long getCacheTtl(String key);

    /**
     * 刷新缓存
     * @param pattern 缓存键模式
     * @return 刷新的键数量
     */
    Long refreshCache(String pattern);
}