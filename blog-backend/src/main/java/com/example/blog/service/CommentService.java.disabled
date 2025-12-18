package com.example.blog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.blog.dto.CommentQuery;
import com.example.blog.dto.CommentResponse;
import com.example.blog.dto.CreateCommentRequest;
import com.example.blog.dto.UpdateCommentRequest;
import com.example.blog.entity.Comment;

import java.util.List;

public interface CommentService extends IService<Comment> {

    /**
     * Create a new comment
     */
    Comment createComment(CreateCommentRequest request, Long userId);

    /**
     * Update comment
     */
    Comment updateComment(Long commentId, UpdateCommentRequest request, Long userId);

    /**
     * Delete comment
     */
    boolean deleteComment(Long commentId, Long userId);

    /**
     * Get comments by article ID with pagination
     */
    IPage<CommentResponse> getCommentsByArticleId(CommentQuery query);

    /**
     * Get nested comments with recursive structure
     */
    List<CommentResponse> getNestedCommentsByArticleId(Long articleId, String sortBy, String sortOrder);

    /**
     * Like a comment
     */
    boolean likeComment(Long commentId, Long userId);

    /**
     * Unlike a comment
     */
    boolean unlikeComment(Long commentId, Long userId);

    /**
     * Check if user has liked a comment
     */
    boolean hasUserLikedComment(Long commentId, Long userId);

    /**
     * Get comment count by article ID
     */
    long getCommentCountByArticleId(Long articleId);

    /**
     * Update article comment count
     */
    void updateArticleCommentCount(Long articleId);

    /**
     * Get comments by user ID
     */
    Page<Comment> getCommentsByUserId(Long userId, Page<Comment> page);
}