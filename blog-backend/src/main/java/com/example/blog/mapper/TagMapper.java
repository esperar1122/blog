package com.example.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blog.entity.Tag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TagMapper extends BaseMapper<Tag> {

    List<Tag> selectTagsWithCount();

    List<Tag> selectPopularTags(@Param("limit") Integer limit);

    List<Tag> selectTagsByArticleId(@Param("articleId") Long articleId);

    boolean existsByName(@Param("name") String name);

    boolean existsByNameAndExcludeId(@Param("name") String name, @Param("excludeId") Long excludeId);

    int updateArticleCount(@Param("id") Long id, @Param("count") Integer count);

    int incrementArticleCount(@Param("id") Long id);

    int decrementArticleCount(@Param("id") Long id);

    List<Tag> selectTagsByNameLike(@Param("name") String name, @Param("limit") Integer limit);
}