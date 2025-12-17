package com.example.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.blog.dto.CommentQuery;
import com.example.blog.dto.CommentResponse;
import com.example.blog.dto.CreateCommentRequest;
import com.example.blog.dto.UpdateCommentRequest;
import com.example.blog.entity.Article;
import com.example.blog.entity.Comment;
import com.example.blog.entity.CommentLike;
import com.example.blog.entity.User;
import com.example.blog.exception.BusinessException;
import com.example.blog.mapper.ArticleMapper;
import com.example.blog.mapper.CommentLikeMapper;
import com.example.blog.mapper.CommentMapper;
import com.example.blog.mapper.UserMapper;
import com.example.blog.service.CommentService;
import com.example.blog.service.NotificationService;
import com.example.blog.service.SensitiveWordService;
import com.example.blog.util.HtmlUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    private final CommentMapper commentMapper;
    private final CommentLikeMapper commentLikeMapper;
    private final UserMapper userMapper;
    private final ArticleMapper articleMapper;
    private final NotificationService notificationService;
    private final SensitiveWordService sensitiveWordService;

    private static final int MAX_NESTING_LEVEL = 5;

    @Override
    @Transactional
    public Comment createComment(CreateCommentRequest request, Long userId) {
        // Validate article exists
        Article article = articleMapper.selectById(request.getArticleId());
        if (article == null || article.getDeleted()) {
            throw new BusinessException("文章不存在");
        }

        // Validate user exists
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // Handle parent comment validation
        Comment parentComment = null;
        Integer level = 1;
        if (request.getParentId() != null) {
            parentComment = commentMapper.selectById(request.getParentId());
            if (parentComment == null || !parentComment.getArticleId().equals(request.getArticleId())) {
                throw new BusinessException("父评论不存在或属于不同文章");
            }
            level = parentComment.getLevel() + 1;
            if (level > MAX_NESTING_LEVEL) {
                throw new BusinessException("评论层级不能超过" + MAX_NESTING_LEVEL + "层");
            }
        }

        // Check for sensitive words
        String cleanedContent = HtmlUtils.cleanHtml(request.getContent());

        // Check for blocked words
        if (sensitiveWordService.containsBlockedWords(cleanedContent)) {
            throw new BusinessException("评论内容包含不当词汇，无法发布");
        }

        // Filter sensitive words
        String filteredContent = sensitiveWordService.filterSensitiveWords(cleanedContent);

        // Create comment
        Comment comment = new Comment();
        comment.setContent(filteredContent);
        comment.setArticleId(request.getArticleId());
        comment.setUserId(userId);
        comment.setParentId(request.getParentId());
        comment.setLevel(level);
        comment.setStatus(Comment.Status.NORMAL.getValue());

        // Save comment
        save(comment);

        // Update article comment count
        updateArticleCommentCount(request.getArticleId());

        // Send notification if it's a reply
        if (parentComment != null && !parentComment.getUserId().equals(userId)) {
            notificationService.sendCommentReplyNotification(parentComment.getUserId(), comment.getId(), article.getId());
        } else if (parentComment == null && !article.getAuthorId().equals(userId)) {
            // Notify article author for new comment
            notificationService.sendNewCommentNotification(article.getAuthorId(), comment.getId(), article.getId());
        }

        return comment;
    }

    @Override
    @Transactional
    public Comment updateComment(Long commentId, UpdateCommentRequest request, Long userId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }

        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException("只能编辑自己的评论");
        }

        if (comment.isDeleted()) {
            throw new BusinessException("已删除的评论不能编辑");
        }

        // Check time limit for editing (30 minutes)
        LocalDateTime thirtyMinutesAgo = LocalDateTime.now().minusMinutes(30);
        if (comment.getCreateTime().isBefore(thirtyMinutesAgo)) {
            throw new BusinessException("评论发布超过30分钟后无法编辑");
        }

        // Check for sensitive words in updated content
        String cleanedContent = HtmlUtils.cleanHtml(request.getContent());

        // Check for blocked words
        if (sensitiveWordService.containsBlockedWords(cleanedContent)) {
            throw new BusinessException("评论内容包含不当词汇，无法更新");
        }

        // Filter sensitive words
        String filteredContent = sensitiveWordService.filterSensitiveWords(cleanedContent);

        comment.setContent(filteredContent);
        comment.markAsEdited();
        updateById(comment);

        return comment;
    }

    @Override
    @Transactional
    public boolean deleteComment(Long commentId, Long userId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }

        // Check permissions: comment author or article author can delete
        Article article = articleMapper.selectById(comment.getArticleId());
        if (!comment.getUserId().equals(userId) && !article.getAuthorId().equals(userId)) {
            throw new BusinessException("无权删除此评论");
        }

        // Soft delete
        comment.setStatus(Comment.Status.DELETED.getValue());
        comment.setContent("该评论已被删除");
        updateById(comment);

        // Update article comment count
        updateArticleCommentCount(comment.getArticleId());

        return true;
    }

    @Override
    public IPage<CommentResponse> getCommentsByArticleId(CommentQuery query) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getArticleId, query.getArticleId())
               .eq(Comment::getStatus, Comment.Status.NORMAL.getValue())
               .isNull(Comment::getParentId); // Only get top-level comments

        // Apply sorting
        if ("likeCount".equals(query.getSortBy())) {
            wrapper.orderByDesc(Comment::getLikeCount);
        } else {
            wrapper.orderByDesc(Comment::getCreateTime);
        }

        Page<Comment> page = new Page<>(query.getPage(), query.getSize());
        IPage<Comment> commentPage = commentMapper.selectPage(page, wrapper);

        // Convert to response DTOs
        IPage<CommentResponse> responsePage = new Page<>(commentPage.getCurrent(), commentPage.getSize(), commentPage.getTotal());
        List<CommentResponse> responses = commentPage.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        responsePage.setRecords(responses);

        return responsePage;
    }

    @Override
    public List<CommentResponse> getNestedCommentsByArticleId(Long articleId, String sortBy, String sortOrder) {
        // Get all comments for the article
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getArticleId, articleId)
               .eq(Comment::getStatus, Comment.Status.NORMAL.getValue());

        // Apply sorting
        if ("likeCount".equals(sortBy)) {
            if ("asc".equals(sortOrder)) {
                wrapper.orderByAsc(Comment::getLikeCount);
            } else {
                wrapper.orderByDesc(Comment::getLikeCount);
            }
        } else {
            if ("asc".equals(sortOrder)) {
                wrapper.orderByAsc(Comment::getCreateTime);
            } else {
                wrapper.orderByDesc(Comment::getCreateTime);
            }
        }

        List<Comment> allComments = commentMapper.selectList(wrapper);

        // Convert to response DTOs
        List<CommentResponse> responses = allComments.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        // Build nested structure
        return buildCommentTree(responses);
    }

    @Override
    @Transactional
    public boolean likeComment(Long commentId, Long userId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null || comment.isDeleted()) {
            throw new BusinessException("评论不存在或已被删除");
        }

        // Check if already liked
        if (commentLikeMapper.existsByCommentIdAndUserId(commentId, userId)) {
            throw new BusinessException("已经点赞过了");
        }

        // Create like record
        CommentLike commentLike = new CommentLike(commentId, userId);
        commentLikeMapper.insert(commentLike);

        // Update comment like count
        comment.incrementLikeCount();
        updateById(comment);

        return true;
    }

    @Override
    @Transactional
    public boolean unlikeComment(Long commentId, Long userId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }

        // Check if liked
        if (!commentLikeMapper.existsByCommentIdAndUserId(commentId, userId)) {
            throw new BusinessException("尚未点赞");
        }

        // Delete like record
        commentLikeMapper.deleteByCommentIdAndUserId(commentId, userId);

        // Update comment like count
        comment.decrementLikeCount();
        updateById(comment);

        return true;
    }

    @Override
    public boolean hasUserLikedComment(Long commentId, Long userId) {
        return commentLikeMapper.existsByCommentIdAndUserId(commentId, userId);
    }

    @Override
    public long getCommentCountByArticleId(Long articleId) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getArticleId, articleId)
               .eq(Comment::getStatus, Comment.Status.NORMAL.getValue());
        return count(wrapper);
    }

    @Override
    @Transactional
    public void updateArticleCommentCount(Long articleId) {
        long count = getCommentCountByArticleId(articleId);

        LambdaUpdateWrapper<Article> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Article::getId, articleId)
                    .set(Article::getCommentCount, (int) count);
        articleMapper.update(null, updateWrapper);
    }

    @Override
    public Page<Comment> getCommentsByUserId(Long userId, Page<Comment> page) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getUserId, userId)
               .eq(Comment::getStatus, Comment.Status.NORMAL.getValue())
               .orderByDesc(Comment::getCreateTime);

        return commentMapper.selectPage(page, wrapper);
    }

    private CommentResponse convertToResponse(Comment comment) {
        CommentResponse response = new CommentResponse();
        response.setId(comment.getId());
        response.setContent(comment.getContent());
        response.setArticleId(comment.getArticleId());
        response.setUserId(comment.getUserId());
        response.setParentId(comment.getParentId());
        response.setLevel(comment.getLevel());
        response.setLikeCount(comment.getLikeCount());
        response.setStatus(comment.getStatus());
        response.setCreateTime(comment.getCreateTime());
        response.setUpdateTime(comment.getUpdateTime());

        // Set user info
        User user = userMapper.selectById(comment.getUserId());
        if (user != null) {
            response.setUserName(user.getNickname() != null ? user.getNickname() : user.getUsername());
            response.setUserAvatar(user.getAvatar());
        }

        return response;
    }

    private List<CommentResponse> buildCommentTree(List<CommentResponse> comments) {
        Map<Long, CommentResponse> commentMap = new HashMap<>();
        List<CommentResponse> rootComments = new ArrayList<>();

        // Put all comments in map
        for (CommentResponse comment : comments) {
            commentMap.put(comment.getId(), comment);
        }

        // Build tree
        for (CommentResponse comment : comments) {
            if (comment.getParentId() == null) {
                rootComments.add(comment);
            } else {
                CommentResponse parent = commentMap.get(comment.getParentId());
                if (parent != null) {
                    if (parent.getReplies() == null) {
                        parent.setReplies(new ArrayList<>());
                    }
                    parent.getReplies().add(comment);
                }
            }
        }

        return rootComments;
    }
}