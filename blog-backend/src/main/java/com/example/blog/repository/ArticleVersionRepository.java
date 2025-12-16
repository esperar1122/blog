package com.example.blog.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blog.entity.ArticleVersion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ArticleVersionRepository extends BaseMapper<ArticleVersion> {

    @Select("SELECT * FROM t_article_version WHERE article_id = #{articleId} ORDER BY version_number DESC")
    List<ArticleVersion> findByArticleIdOrderByVersionNumberDesc(@Param("articleId") Long articleId);

    @Select("SELECT * FROM t_article_version WHERE article_id = #{articleId} AND version_number = #{versionNumber}")
    ArticleVersion findByArticleIdAndVersionNumber(@Param("articleId") Long articleId, @Param("versionNumber") Integer versionNumber);

    @Select("SELECT MAX(version_number) FROM t_article_version WHERE article_id = #{articleId}")
    Integer findMaxVersionNumberByArticleId(@Param("articleId") Long articleId);

    @Select("SELECT COUNT(*) FROM t_article_version WHERE article_id = #{articleId}")
    int countByArticleId(@Param("articleId") Long articleId);
}