package com.example.blog.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();

        // 系统监控相关缓存
        cacheManager.setCacheNames(
            "systemStatus",    // 系统状态缓存，5秒
            "systemMetrics",   // 系统指标缓存，3秒
            "databaseStatus",  // 数据库状态缓存，5秒
            "redisStatus",     // Redis状态缓存，5秒
            "healthCheck"      // 健康检查缓存，2秒
        );

        return cacheManager;
    }
}