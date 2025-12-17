package com.example.blog.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blog.common.Result;
import com.example.blog.dto.CommentQuery;
import com.example.blog.dto.CommentResponse;
import com.example.blog.dto.CreateCommentRequest;
import com.example.blog.dto.UpdateCommentRequest;
import com.example.blog.entity.Comment;
import com.example.blog.entity.User;
import com.example.blog.service.CommentService;
import com.example.blog.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public Result<CommentResponse> createComment(@Valid @RequestBody CreateCommentRequest request,
                                               HttpServletRequest httpRequest) {
        Long userId = JwtUtil.getUserIdFromRequest(httpRequest);
        Comment comment = commentService.createComment(request, userId);

        CommentResponse response = convertToResponse(comment);
        return Result.success(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<CommentResponse> updateComment(@PathVariable Long id,
                                               @Valid @RequestBody UpdateCommentRequest request,
                                               HttpServletRequest httpRequest) {
        Long userId = JwtUtil.getUserIdFromRequest(httpRequest);
        Comment comment = commentService.updateComment(id, request, userId);

        CommentResponse response = convertToResponse(comment);
        return Result.success(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> deleteComment(@PathVariable Long id,
                                     HttpServletRequest httpRequest) {
        Long userId = JwtUtil.getUserIdFromRequest(httpRequest);
        commentService.deleteComment(id, userId);
        return Result.success();
    }

    @GetMapping
    public Result<IPage<CommentResponse>> getComments(@Valid CommentQuery query) {
        IPage<CommentResponse> comments = commentService.getCommentsByArticleId(query);
        return Result.success(comments);
    }

    @GetMapping("/nested")
    public Result<List<CommentResponse>> getNestedComments(@RequestParam Long articleId,
                                                          @RequestParam(defaultValue = "createTime") String sortBy,
                                                          @RequestParam(defaultValue = "desc") String sortOrder) {
        List<CommentResponse> comments = commentService.getNestedCommentsByArticleId(articleId, sortBy, sortOrder);
        return Result.success(comments);
    }

    @GetMapping("/articles/{articleId}/all")
    public Result<List<CommentResponse>> getAllCommentsForArticle(@PathVariable Long articleId) {
        List<CommentResponse> comments = commentService.getNestedCommentsByArticleId(articleId, "createTime", "desc");
        return Result.success(comments);
    }

    @GetMapping("/user/me")
    @PreAuthorize("isAuthenticated()")
    public Result<IPage<CommentResponse>> getMyComments(@RequestParam(defaultValue = "1") Integer page,
                                                       @RequestParam(defaultValue = "10") Integer size,
                                                       HttpServletRequest httpRequest) {
        Long userId = JwtUtil.getUserIdFromRequest(httpRequest);
        Page<Comment> commentPage = new Page<>(page, size);
        IPage<Comment> myComments = commentService.getCommentsByUserId(userId, commentPage);

        IPage<CommentResponse> responsePage = new Page<>(myComments.getCurrent(), myComments.getSize(), myComments.getTotal());
        List<CommentResponse> responses = myComments.getRecords().stream()
                .map(this::convertToResponse)
                .toList();
        responsePage.setRecords(responses);

        return Result.success(responsePage);
    }

    @PostMapping("/{id}/like")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> likeComment(@PathVariable Long id,
                                    HttpServletRequest httpRequest) {
        Long userId = JwtUtil.getUserIdFromRequest(httpRequest);
        commentService.likeComment(id, userId);
        return Result.success();
    }

    @DeleteMapping("/{id}/like")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> unlikeComment(@PathVariable Long id,
                                      HttpServletRequest httpRequest) {
        Long userId = JwtUtil.getUserIdFromRequest(httpRequest);
        commentService.unlikeComment(id, userId);
        return Result.success();
    }

    @GetMapping("/{id}/liked")
    @PreAuthorize("isAuthenticated()")
    public Result<Map<String, Boolean>> checkIfLiked(@PathVariable Long id,
                                                    HttpServletRequest httpRequest) {
        Long userId = JwtUtil.getUserIdFromRequest(httpRequest);
        boolean liked = commentService.hasUserLikedComment(id, userId);
        return Result.success(Map.of("liked", liked));
    }

    @GetMapping("/articles/{articleId}/count")
    public Result<Map<String, Long>> getCommentCount(@PathVariable Long articleId) {
        long count = commentService.getCommentCountByArticleId(articleId);
        return Result.success(Map.of("count", count));
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

        return response;
    }
}