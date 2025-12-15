package com.example.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blog.entity.Article;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ArticleMapper extends BaseMapper<Article> {

    IPage<Article> selectArticlesWithDetails(Page<Article> page, @Param("categoryId") Long categoryId,
                                           @Param("tagId") Long tagId, @Param("keyword") String keyword,
                                           @Param("status") String status);

    Article selectArticleWithDetails(@Param("id") Long id);

    List<Article> selectArticlesByAuthor(@Param("authorId") Long authorId, @Param("status") String status, @Param("limit") Integer limit);

    List<Article> selectPopularArticles(@Param("limit") Integer limit);

    List<Article> selectLatestArticles(@Param("limit") Integer limit);

    List<Article> selectRelatedArticles(@Param("id") Long id, @Param("categoryId") Long categoryId, @Param("limit") Integer limit);

    int incrementViewCount(@Param("id") Long id);

    int incrementLikeCount(@Param("id") Long id);

    int decrementLikeCount(@Param("id") Long id);

    int incrementCommentCount(@Param("id") Long id);

    int decrementCommentCount(@Param("id") Long id);

    int updatePublishStatus(@Param("id") Long id, @Param("status") String status);

    List<Article> selectDraftArticles(@Param("authorId") Long authorId);

    List<Article> selectPublishedArticles(@Param("authorId") Long authorId);
}