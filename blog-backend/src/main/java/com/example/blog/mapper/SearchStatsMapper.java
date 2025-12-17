package com.example.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blog.entity.SearchStats;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SearchStatsMapper extends BaseMapper<SearchStats> {

    int incrementSearchCount(@Param("keyword") String keyword, @Param("resultCount") Integer resultCount);

    int updateAvgResultCount(@Param("keyword") String keyword, @Param("avgResultCount") Double avgResultCount);

    List<SearchStats> selectTopSearchKeywords(@Param("limit") int limit);

    IPage<SearchStats> selectByDateRange(
        Page<SearchStats> page,
        @Param("startDate") String startDate,
        @Param("endDate") String endDate
    );

    long getTotalSearchCount();

    List<SearchStats> selectZeroResultKeywords();
}