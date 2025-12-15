package com.example.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blog.entity.ArticleLike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ArticleLikeMapper extends BaseMapper<ArticleLike> {

    boolean existsByArticleIdAndUserId(@Param("articleId") Long articleId, @Param("userId") Long userId);

    List<ArticleLike> selectLikesByUserId(@Param("userId") Long userId, @Param("limit") Integer limit);

    List<ArticleLike> selectLikesByArticleId(@Param("articleId") Long articleId);

    int countLikesByArticleId(@Param("articleId") Long articleId);

    int countLikesByUserId(@Param("userId") Long userId);

    int deleteByArticleIdAndUserId(@Param("articleId") Long articleId, @Param("userId") Long userId);

    int deleteLikesByArticleId(@Param("articleId") Long articleId);

    int deleteLikesByUserId(@Param("userId") Long userId);
}