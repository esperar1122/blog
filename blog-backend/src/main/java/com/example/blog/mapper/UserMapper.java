package com.example.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blog.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    User findByUsername(@Param("username") String username);

    User findByEmail(@Param("email") String email);

    boolean existsByUsername(@Param("username") String username);

    boolean existsByEmail(@Param("email") String email);

    IPage<User> selectUsersWithRole(Page<User> page, @Param("role") String role);

    List<User> selectActiveUsers();

    int incrementArticleCount(@Param("userId") Long userId);

    int decrementArticleCount(@Param("userId") Long userId);

    int updateLastLoginTime(@Param("userId") Long userId);
}