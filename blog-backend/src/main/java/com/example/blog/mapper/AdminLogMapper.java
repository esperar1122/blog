package com.example.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blog.entity.AdminLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AdminLogMapper extends BaseMapper<AdminLog> {
}