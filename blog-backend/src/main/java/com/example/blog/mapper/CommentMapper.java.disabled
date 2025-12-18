package com.example.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blog.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    IPage<Comment> selectCommentsWithDetails(Page<Comment> page, @Param("articleId") Long articleId,
                                            @Param("status") String status);

    List<Comment> selectTopLevelComments(@Param("articleId") Long articleId, @Param("status") String status);

    List<Comment> selectRepliesByParentId(@Param("parentId") Long parentId, @Param("status") String status);

    List<Comment> selectCommentsByUserId(@Param("userId") Long userId, @Param("limit") Integer limit);

    int incrementLikeCount(@Param("id") Long id);

    int decrementLikeCount(@Param("id") Long id);

    int updateStatus(@Param("id") Long id, @Param("status") String status);

    int deleteCommentsByArticleId(@Param("articleId") Long articleId);

    int countCommentsByArticleId(@Param("articleId") Long articleId);

    int countCommentsByUserId(@Param("userId") Long userId);
}