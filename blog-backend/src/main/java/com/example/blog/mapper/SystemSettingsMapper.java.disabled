package com.example.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blog.entity.SystemSettings;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.util.List;

@Mapper
public interface SystemSettingsMapper extends BaseMapper<SystemSettings> {

    @Select("SELECT * FROM t_system_settings WHERE is_public = true")
    List<SystemSettings> selectPublicSettings();

    @Select("SELECT * FROM t_system_settings WHERE setting_key = #{key}")
    SystemSettings selectByKey(String key);

    @Update("UPDATE t_system_settings SET setting_value = #{value}, update_time = NOW() WHERE setting_key = #{key}")
    int updateByKey(String key, String value);
}