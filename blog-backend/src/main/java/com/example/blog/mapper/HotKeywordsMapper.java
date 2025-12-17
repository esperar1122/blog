package com.example.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blog.entity.HotKeywords;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface HotKeywordsMapper extends BaseMapper<HotKeywords> {

    List<HotKeywords> selectTopKeywords(@Param("limit") int limit);

    int updateSearchCount(@Param("keyword") String keyword);

    int updatePositions(@Param("keywords") List<HotKeywords> keywords);

    int resetAllPositions();

    HotKeywords selectByKeyword(@Param("keyword") String keyword);

    int insertOrUpdate(HotKeywords hotKeywords);
}