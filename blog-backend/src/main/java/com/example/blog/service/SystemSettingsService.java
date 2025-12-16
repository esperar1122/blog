package com.example.blog.service;

import com.example.blog.entity.SystemSettings;
import java.util.List;
import java.util.Map;

public interface SystemSettingsService {

    List<SystemSettings> getAllSettings();

    List<SystemSettings> getPublicSettings();

    SystemSettings getSettingByKey(String key);

    void updateSetting(SystemSettings setting);

    void batchUpdateSettings(Map<String, String> settings);

    String getSettingValue(String key, String defaultValue);

    boolean getBooleanSetting(String key, boolean defaultValue);

    int getIntSetting(String key, int defaultValue);

    void resetToDefault(String key);
}