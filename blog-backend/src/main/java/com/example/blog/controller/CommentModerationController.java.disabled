package com.example.blog.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blog.dto.BlacklistRequest;
import com.example.blog.dto.CommentModerationRequest;
import com.example.blog.dto.CommentReportRequest;
import com.example.blog.entity.CommentBlacklist;
import com.example.blog.entity.CommentReport;
import com.example.blog.entity.SensitiveWord;
import com.example.blog.service.CommentModerationService;
import com.example.blog.service.CommentService;
import com.example.blog.service.SensitiveWordService;
import com.example.blog.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "评论管理", description = "评论管理相关接口")
public class CommentModerationController {

    private final CommentModerationService commentModerationService;
    private final SensitiveWordService sensitiveWordService;
    private final CommentService commentService;

    @PostMapping("/comments/{id}/report")
    @Operation(summary = "举报评论")
    public Result<CommentReport> reportComment(
            @Parameter(description = "评论ID") @PathVariable Long id,
            @Valid @RequestBody CommentReportRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long reporterId = Long.parseLong(userDetails.getUsername());
        CommentReport report = commentModerationService.reportComment(id, reporterId, request);
        return Result.success(report);
    }

    @GetMapping("/comments/reports")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取举报列表")
    public Result<IPage<CommentReport>> getReports(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "状态筛选") @RequestParam(required = false) String status,
            @Parameter(description = "关键词搜索") @RequestParam(required = false) String keyword) {

        Page<CommentReport> pageParam = new Page<>(page, size);
        IPage<CommentReport> result = commentModerationService.getReports(pageParam, status, keyword);
        return Result.success(result);
    }

    @PutMapping("/comments/reports/{id}/review")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "审核举报")
    public Result<Void> reviewReport(
            @Parameter(description = "举报ID") @PathVariable Long id,
            @Valid @RequestBody CommentModerationRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long reviewerId = Long.parseLong(userDetails.getUsername());
        commentModerationService.reviewReport(id, reviewerId, request);
        return Result.success();
    }

    @PutMapping("/comments/{id}/moderate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "审核评论")
    public Result<Void> moderateComment(
            @Parameter(description = "评论ID") @PathVariable Long id,
            @Valid @RequestBody CommentModerationRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long moderatorId = Long.parseLong(userDetails.getUsername());
        commentModerationService.moderateComment(id, request.getStatus(), moderatorId, request.getReviewNote());
        return Result.success();
    }

    @PostMapping("/comments/batch-moderate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "批量审核评论")
    public Result<Void> batchModerateComments(
            @RequestBody Map<String, Object> request,
            @AuthenticationPrincipal UserDetails userDetails) {

        @SuppressWarnings("unchecked")
        List<Long> commentIds = (List<Long>) request.get("commentIds");
        String action = (String) request.get("action");
        String reason = (String) request.get("reason");

        Long moderatorId = Long.parseLong(userDetails.getUsername());
        commentModerationService.batchModerateComments(commentIds, action, moderatorId, reason);
        return Result.success();
    }

    @PostMapping("/users/{id}/blacklist")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "添加用户到黑名单")
    public Result<CommentBlacklist> addToBlacklist(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @Valid @RequestBody BlacklistRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        request.setUserId(id);
        Long adminId = Long.parseLong(userDetails.getUsername());
        CommentBlacklist blacklist = commentModerationService.addToBlacklist(request, adminId);
        return Result.success(blacklist);
    }

    @DeleteMapping("/users/{id}/blacklist")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "从黑名单移除用户")
    public Result<Void> removeFromBlacklist(
            @Parameter(description = "用户ID") @PathVariable Long id) {

        commentModerationService.removeFromBlacklist(id);
        return Result.success();
    }

    @GetMapping("/blacklist")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取黑名单列表")
    public Result<List<CommentBlacklist>> getBlacklistedUsers() {
        List<CommentBlacklist> blacklistedUsers = commentModerationService.getBlacklistedUsers();
        return Result.success(blacklistedUsers);
    }

    @GetMapping("/sensitive-words")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取敏感词列表")
    public Result<List<SensitiveWord>> getSensitiveWords(
            @Parameter(description = "敏感词类型") @RequestParam(required = false) String type) {

        List<SensitiveWord> words;
        if (type != null && !type.isEmpty()) {
            words = sensitiveWordService.getWordsByType(type);
        } else {
            words = sensitiveWordService.list();
        }
        return Result.success(words);
    }

    @PostMapping("/sensitive-words")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "添加敏感词")
    public Result<SensitiveWord> addSensitiveWord(@Valid @RequestBody SensitiveWordRequest request) {
        SensitiveWord word = sensitiveWordService.addSensitiveWord(request);
        return Result.success(word);
    }

    @PutMapping("/sensitive-words/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "更新敏感词")
    public Result<SensitiveWord> updateSensitiveWord(
            @Parameter(description = "敏感词ID") @PathVariable Long id,
            @Valid @RequestBody SensitiveWordRequest request) {

        SensitiveWord word = sensitiveWordService.updateSensitiveWord(id, request);
        return Result.success(word);
    }

    @DeleteMapping("/sensitive-words/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "删除敏感词")
    public Result<Void> deleteSensitiveWord(@Parameter(description = "敏感词ID") @PathVariable Long id) {
        sensitiveWordService.removeById(id);
        return Result.success();
    }

    @PostMapping("/comments/filter")
    @Operation(summary = "过滤敏感词")
    public Result<Map<String, Object>> filterSensitiveWords(
            @RequestBody Map<String, String> request) {

        String text = request.get("text");
        Map<String, Object> result = Map.of(
            "filteredText", sensitiveWordService.filterSensitiveWords(text),
            "containsBlocked", sensitiveWordService.containsBlockedWords(text),
            "warningWords", sensitiveWordService.getWarningWords(text)
        );
        return Result.success(result);
    }

    @GetMapping("/comments/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取评论统计数据")
    public Result<Map<String, Object>> getCommentStatistics() {
        Map<String, Object> statistics = commentModerationService.getCommentStatistics();
        return Result.success(statistics);
    }

    @GetMapping("/comments/reports/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取举报统计数据")
    public Result<Map<String, Object>> getReportStatistics() {
        Map<String, Object> statistics = commentModerationService.getReportStatistics();
        return Result.success(statistics);
    }
}