package com.example.blog.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blog.entity.ArticleOperationLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ArticleOperationLogRepository extends BaseMapper<ArticleOperationLog> {

    @Select("SELECT * FROM t_article_operation_log WHERE article_id = #{articleId} ORDER BY create_time DESC")
    List<ArticleOperationLog> findByArticleIdOrderByCreateTimeDesc(@Param("articleId") Long articleId);

    @Select("SELECT * FROM t_article_operation_log WHERE article_id = #{articleId} AND operation_type = #{operationType} ORDER BY create_time DESC")
    List<ArticleOperationLog> findByArticleIdAndOperationType(@Param("articleId") Long articleId, @Param("operationType") String operationType);

    @Select("SELECT * FROM t_article_operation_log WHERE operator_id = #{operatorId} ORDER BY create_time DESC")
    IPage<ArticleOperationLog> findByOperatorId(Page<ArticleOperationLog> page, @Param("operatorId") Long operatorId);

    @Select("SELECT * FROM t_article_operation_log WHERE article_id = #{articleId} AND create_time >= #{startTime} AND create_time <= #{endTime} ORDER BY create_time DESC")
    List<ArticleOperationLog> findByArticleIdAndTimeRange(@Param("articleId") Long articleId,
                                                        @Param("startTime") LocalDateTime startTime,
                                                        @Param("endTime") LocalDateTime endTime);

    @Select("SELECT COUNT(*) FROM t_article_operation_log WHERE article_id = #{articleId}")
    int countByArticleId(@Param("articleId") Long articleId);

    @Select("SELECT COUNT(*) FROM t_article_operation_log WHERE article_id = #{articleId} AND operation_type = #{operationType}")
    int countByArticleIdAndOperationType(@Param("articleId") Long articleId, @Param("operationType") String operationType);
}