package com.example.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blog.entity.SearchHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface SearchHistoryMapper extends BaseMapper<SearchHistory> {

    IPage<SearchHistory> selectByUserId(Page<SearchHistory> page, @Param("userId") Long userId);

    List<SearchHistory> selectRecentByUserId(@Param("userId") Long userId, @Param("limit") int limit);

    int deleteByUserId(@Param("userId") Long userId);

    int deleteByUserIdBeforeDate(@Param("userId") Long userId, @Param("beforeDate") LocalDateTime beforeDate);

    List<String> selectPopularKeywordsByUserId(@Param("userId") Long userId, @Param("limit") int limit);
}