package com.example.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.blog.entity.SystemSettings;
import com.example.blog.exception.BusinessException;
import com.example.blog.mapper.SystemSettingsMapper;
import com.example.blog.service.SystemSettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class SystemSettingsServiceImpl implements SystemSettingsService {

    private final SystemSettingsMapper systemSettingsMapper;

    private final Map<String, String> settingsCache = new ConcurrentHashMap<>();
    private static final long CACHE_TTL = 10 * 60 * 1000;
    private volatile long lastCacheTime = 0;

    @Override
    public List<SystemSettings> getAllSettings() {
        QueryWrapper<SystemSettings> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByAsc("setting_key");
        return systemSettingsMapper.selectList(queryWrapper);
    }

    @Override
    public List<SystemSettings> getPublicSettings() {
        refreshCacheIfNeeded();
        return systemSettingsMapper.selectPublicSettings();
    }

    @Override
    public SystemSettings getSettingByKey(String key) {
        if (!StringUtils.hasText(key)) {
            return null;
        }
        return systemSettingsMapper.selectByKey(key);
    }

    @Override
    @Transactional
    public void updateSetting(SystemSettings setting) {
        if (!StringUtils.hasText(setting.getSettingKey())) {
            throw new BusinessException("设置键不能为空");
        }

        SystemSettings existing = getSettingByKey(setting.getSettingKey());
        if (existing != null) {
            existing.setSettingValue(setting.getSettingValue());
            existing.setDescription(setting.getDescription());
            existing.setType(setting.getType());
            existing.setIsPublic(setting.getIsPublic());
            systemSettingsMapper.updateById(existing);
        } else {
            systemSettingsMapper.insert(setting);
        }

        invalidateCache();
        log.info("更新系统设置: {} = {}", setting.getSettingKey(), setting.getSettingValue());
    }

    @Override
    @Transactional
    public void batchUpdateSettings(Map<String, String> settings) {
        if (settings == null || settings.isEmpty()) {
            return;
        }

        for (Map.Entry<String, String> entry : settings.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            SystemSettings setting = getSettingByKey(key);
            if (setting != null) {
                setting.setSettingValue(value);
                systemSettingsMapper.updateById(setting);
            }
        }

        invalidateCache();
        log.info("批量更新系统设置，数量: {}", settings.size());
    }

    @Override
    public String getSettingValue(String key, String defaultValue) {
        refreshCacheIfNeeded();
        return settingsCache.getOrDefault(key, defaultValue);
    }

    @Override
    public boolean getBooleanSetting(String key, boolean defaultValue) {
        String value = getSettingValue(key, String.valueOf(defaultValue));
        return Boolean.parseBoolean(value);
    }

    @Override
    public int getIntSetting(String key, int defaultValue) {
        String value = getSettingValue(key, String.valueOf(defaultValue));
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.warn("设置值不是有效整数: {} = {}", key, value);
            return defaultValue;
        }
    }

    @Override
    @Transactional
    public void resetToDefault(String key) {
        SystemSettings setting = getSettingByKey(key);
        if (setting == null) {
            throw new BusinessException("设置不存在: " + key);
        }

        Map<String, String> defaultSettings = getDefaultSettings();
        String defaultValue = defaultSettings.get(key);
        if (defaultValue == null) {
            throw new BusinessException("没有找到默认值: " + key);
        }

        setting.setSettingValue(defaultValue);
        systemSettingsMapper.updateById(setting);
        invalidateCache();

        log.info("重置系统设置: {} = {}", key, defaultValue);
    }

    private void refreshCacheIfNeeded() {
        long currentTime = System.currentTimeMillis();
        if (settingsCache.isEmpty() || (currentTime - lastCacheTime) > CACHE_TTL) {
            loadSettingsToCache();
        }
    }

    private void loadSettingsToCache() {
        try {
            List<SystemSettings> allSettings = getAllSettings();
            settingsCache.clear();

            for (SystemSettings setting : allSettings) {
                if (StringUtils.hasText(setting.getSettingKey())) {
                    settingsCache.put(setting.getSettingKey(), setting.getSettingValue());
                }
            }

            lastCacheTime = System.currentTimeMillis();
            log.debug("刷新系统设置缓存，当前设置数量: {}", settingsCache.size());
        } catch (Exception e) {
            log.error("加载系统设置到缓存失败", e);
        }
    }

    private void invalidateCache() {
        settingsCache.clear();
        lastCacheTime = 0;
    }

    private Map<String, String> getDefaultSettings() {
        Map<String, String> defaults = new ConcurrentHashMap<>();

        defaults.put("site.title", "博客系统");
        defaults.put("site.description", "一个简单的博客管理系统");
        defaults.put("site.keywords", "博客,文章,评论");
        defaults.put("site.author", "管理员");

        defaults.put("comment.enabled", "true");
        defaults.put("comment.require_login", "true");
        defaults.put("comment.auto_approve", "false");
        defaults.put("comment.max_length", "1000");

        defaults.put("article.per_page", "10");
        defaults.put("article.allow_comment", "true");
        defaults.put("article.auto_save", "true");

        defaults.put("upload.max_file_size", "5242880");
        defaults.put("upload.allowed_types", "jpg,jpeg,png,gif,pdf,doc,docx");
        defaults.put("upload.image_max_width", "1920");
        defaults.put("upload.image_max_height", "1080");

        defaults.put("email.enabled", "false");
        defaults.put("email.smtp_host", "");
        defaults.put("email.smtp_port", "587");
        defaults.put("email.username", "");
        defaults.put("email.from_address", "");

        defaults.put("security.login_attempts", "5");
        defaults.put("security.lockout_duration", "1800");
        defaults.put("security.session_timeout", "3600");

        defaults.put("sensitive_word.enabled", "true");
        defaults.put("sensitive_word.replace_with", "***");

        defaults.put("cache.enabled", "true");
        defaults.put("cache.ttl", "300");

        return defaults;
    }
}