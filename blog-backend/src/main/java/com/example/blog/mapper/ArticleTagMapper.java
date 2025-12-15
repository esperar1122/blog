package com.example.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blog.entity.ArticleTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ArticleTagMapper extends BaseMapper<ArticleTag> {

    List<Long> selectTagIdsByArticleId(@Param("articleId") Long articleId);

    List<Long> selectArticleIdsByTagId(@Param("tagId") Long tagId);

    int insertArticleTags(@Param("articleId") Long articleId, @Param("tagIds") List<Long> tagIds);

    int deleteArticleTagsByArticleId(@Param("articleId") Long articleId);

    int deleteArticleTagsByTagId(@Param("tagId") Long tagId);

    int deleteArticleTag(@Param("articleId") Long articleId, @Param("tagId") Long tagId);

    boolean existsArticleTag(@Param("articleId") Long articleId, @Param("tagId") Long tagId);

    List<ArticleTag> selectArticleTagsByArticleId(@Param("articleId") Long articleId);
}