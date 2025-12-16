package com.example.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blog.dto.request.ContentQueryRequest;
import com.example.blog.dto.request.ArticleReviewRequest;
import com.example.blog.dto.request.BatchOperationRequest;
import com.example.blog.dto.response.ContentStatsResponse;
import com.example.blog.dto.response.ExportResponse;
import com.example.blog.entity.Article;
import com.example.blog.entity.Comment;
import com.example.blog.entity.User;
import com.example.blog.exception.BusinessException;
import com.example.blog.mapper.ArticleMapper;
import com.example.blog.mapper.CommentMapper;
import com.example.blog.mapper.UserMapper;
import com.example.blog.service.AdminContentService;
import com.example.blog.service.SensitiveWordService;
import com.example.blog.service.AdminLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminContentServiceImpl implements AdminContentService {

    private final ArticleMapper articleMapper;
    private final CommentMapper commentMapper;
    private final UserMapper userMapper;
    private final SensitiveWordService sensitiveWordService;
    private final AdminLogService adminLogService;

    @Override
    public IPage<Article> getArticleList(ContentQueryRequest request) {
        Page<Article> page = new Page<>(request.getPage(), request.getSize());
        QueryWrapper<Article> queryWrapper = new QueryWrapper<>();

        if (StringUtils.hasText(request.getStatus())) {
            queryWrapper.eq("status", request.getStatus());
        }

        if (StringUtils.hasText(request.getKeyword())) {
            String keyword = request.getKeyword();
            queryWrapper.and(wrapper -> wrapper
                .like("title", keyword)
                .or()
                .like("content", keyword)
            );
        }

        if (request.getCategoryId() != null) {
            queryWrapper.eq("category_id", request.getCategoryId());
        }

        if (StringUtils.hasText(request.getStartTime())) {
            queryWrapper.ge("create_time", request.getStartTime());
        }

        if (StringUtils.hasText(request.getEndTime())) {
            queryWrapper.le("create_time", request.getEndTime());
        }

        queryWrapper.orderByDesc("create_time");

        IPage<Article> result = articleMapper.selectPage(page, queryWrapper);

        for (Article article : result.getRecords()) {
            User author = userMapper.selectById(article.getAuthorId());
            if (author != null) {
                article.setAuthorName(author.getUsername());
            }
        }

        return result;
    }

    @Override
    public IPage<Comment> getCommentList(ContentQueryRequest request) {
        Page<Comment> page = new Page<>(request.getPage(), request.getSize());
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();

        if (StringUtils.hasText(request.getStatus())) {
            queryWrapper.eq("status", request.getStatus());
        }

        if (StringUtils.hasText(request.getKeyword())) {
            String keyword = request.getKeyword();
            queryWrapper.like("content", keyword);
        }

        if (request.getArticleId() != null) {
            queryWrapper.eq("article_id", request.getArticleId());
        }

        if (StringUtils.hasText(request.getStartTime())) {
            queryWrapper.ge("create_time", request.getStartTime());
        }

        if (StringUtils.hasText(request.getEndTime())) {
            queryWrapper.le("create_time", request.getEndTime());
        }

        queryWrapper.orderByDesc("create_time");

        IPage<Comment> result = commentMapper.selectPage(page, queryWrapper);

        for (Comment comment : result.getRecords()) {
            User user = userMapper.selectById(comment.getUserId());
            if (user != null) {
                comment.setUserName(user.getUsername());
            }
        }

        return result;
    }

    @Override
    @Transactional
    public Article reviewArticle(ArticleReviewRequest request, Long adminId) {
        Article article = articleMapper.selectById(request.getId());
        if (article == null) {
            throw new BusinessException("文章不存在");
        }

        String oldStatus = article.getStatus();

        article.setTitle(request.getTitle());
        article.setContent(request.getContent());
        article.setSummary(request.getSummary());
        article.setCoverImage(request.getCoverImage());
        article.setStatus(request.getStatus());
        article.setCategoryId(request.getCategoryId());
        article.setUpdateTime(LocalDateTime.now());

        if (Article.Status.PUBLISHED.getValue().equals(request.getStatus()) &&
            !Article.Status.PUBLISHED.getValue().equals(oldStatus)) {
            article.setPublishTime(LocalDateTime.now());
        }

        int result = articleMapper.updateById(article);
        if (result <= 0) {
            throw new BusinessException("文章审核失败");
        }

        adminLogService.log(adminId, "REVIEW_ARTICLE",
            String.format("审核文章 ID:%d, 状态从 %s 变更为 %s",
                request.getId(), oldStatus, request.getStatus()));

        return article;
    }

    @Override
    @Transactional
    public void batchOperateArticles(BatchOperationRequest request, Long adminId) {
        if (request.getIds().isEmpty()) {
            throw new BusinessException("请选择要操作的文章");
        }

        String operation = request.getOperationType();
        List<Long> ids = request.getIds();

        UpdateWrapper<Article> updateWrapper = new UpdateWrapper<>();
        updateWrapper.in("id", ids);

        switch (operation) {
            case "delete":
                updateWrapper.set("status", Article.Status.DELETED.getValue());
                adminLogService.log(adminId, "BATCH_DELETE_ARTICLE",
                    String.format("批量删除文章，数量: %d", ids.size()));
                break;
            case "publish":
                updateWrapper.set("status", Article.Status.PUBLISHED.getValue())
                          .set("publish_time", LocalDateTime.now());
                adminLogService.log(adminId, "BATCH_PUBLISH_ARTICLE",
                    String.format("批量发布文章，数量: %d", ids.size()));
                break;
            case "unpublish":
                updateWrapper.set("status", Article.Status.DRAFT.getValue());
                adminLogService.log(adminId, "BATCH_UNPUBLISH_ARTICLE",
                    String.format("批量取消发布文章，数量: %d", ids.size()));
                break;
            case "update_status":
                if (!StringUtils.hasText(request.getNewStatus())) {
                    throw new BusinessException("请指定新的状态");
                }
                updateWrapper.set("status", request.getNewStatus());
                adminLogService.log(adminId, "BATCH_UPDATE_ARTICLE_STATUS",
                    String.format("批量更新文章状态为 %s，数量: %d", request.getNewStatus(), ids.size()));
                break;
            default:
                throw new BusinessException("不支持的操作类型");
        }

        articleMapper.update(null, updateWrapper);
    }

    @Override
    @Transactional
    public void batchOperateComments(BatchOperationRequest request, Long adminId) {
        if (request.getIds().isEmpty()) {
            throw new BusinessException("请选择要操作的评论");
        }

        String operation = request.getOperationType();
        List<Long> ids = request.getIds();

        UpdateWrapper<Comment> updateWrapper = new UpdateWrapper<>();
        updateWrapper.in("id", ids);

        switch (operation) {
            case "delete":
                updateWrapper.set("status", Comment.Status.DELETED.getValue());
                adminLogService.log(adminId, "BATCH_DELETE_COMMENT",
                    String.format("批量删除评论，数量: %d", ids.size()));
                break;
            default:
                throw new BusinessException("不支持的操作类型");
        }

        commentMapper.update(null, updateWrapper);
    }

    @Override
    public ExportResponse exportArticles(ContentQueryRequest filters, String format) {
        QueryWrapper<Article> queryWrapper = new QueryWrapper<>();

        if (StringUtils.hasText(filters.getStatus())) {
            queryWrapper.eq("status", filters.getStatus());
        }

        if (StringUtils.hasText(filters.getKeyword())) {
            String keyword = filters.getKeyword();
            queryWrapper.and(wrapper -> wrapper
                .like("title", keyword)
                .or()
                .like("content", keyword)
            );
        }

        List<Article> articles = articleMapper.selectList(queryWrapper);

        String fileName = "articles_export_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + "." + format;
        String content = generateExportContent(articles, format);

        return ExportResponse.builder()
                .fileName(fileName)
                .downloadUrl("/admin/download/" + fileName)
                .fileSize((long) content.getBytes().length)
                .format(format)
                .recordCount(articles.size())
                .exportTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .build();
    }

    @Override
    public ExportResponse exportComments(ContentQueryRequest filters, String format) {
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();

        if (StringUtils.hasText(filters.getStatus())) {
            queryWrapper.eq("status", filters.getStatus());
        }

        if (StringUtils.hasText(filters.getKeyword())) {
            queryWrapper.like("content", filters.getKeyword());
        }

        List<Comment> comments = commentMapper.selectList(queryWrapper);

        String fileName = "comments_export_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + "." + format;
        String content = generateCommentExportContent(comments, format);

        return ExportResponse.builder()
                .fileName(fileName)
                .downloadUrl("/admin/download/" + fileName)
                .fileSize((long) content.getBytes().length)
                .format(format)
                .recordCount(comments.size())
                .exportTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .build();
    }

    @Override
    public ContentStatsResponse getContentStats() {
        QueryWrapper<Article> articleQuery = new QueryWrapper<>();
        QueryWrapper<Comment> commentQuery = new QueryWrapper<>();

        Long totalArticles = articleMapper.selectCount(articleQuery);

        articleQuery.eq("status", Article.Status.PUBLISHED.getValue());
        Long publishedArticles = articleMapper.selectCount(articleQuery);

        articleQuery.clear();
        articleQuery.eq("status", Article.Status.DRAFT.getValue());
        Long draftArticles = articleMapper.selectCount(articleQuery);

        Long totalComments = commentMapper.selectCount(commentQuery);

        commentQuery.eq("status", Comment.Status.NORMAL.getValue());
        Long activeComments = commentQuery.selectCount(commentQuery);

        commentQuery.clear();
        commentQuery.eq("status", Comment.Status.DELETED.getValue());
        Long deletedComments = commentQuery.selectCount(commentQuery);

        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        articleQuery.clear();
        articleQuery.ge("create_time", today);
        Long todayArticles = articleMapper.selectCount(articleQuery);

        commentQuery.clear();
        commentQuery.ge("create_time", today);
        Long todayComments = commentQuery.selectCount(commentQuery);

        return ContentStatsResponse.builder()
                .totalArticles(totalArticles)
                .publishedArticles(publishedArticles)
                .draftArticles(draftArticles)
                .totalComments(totalComments)
                .activeComments(activeComments)
                .deletedComments(deletedComments)
                .todayArticles(todayArticles)
                .todayComments(todayComments)
                .build();
    }

    @Override
    public boolean checkContent(String content) {
        return sensitiveWordService.containsSensitiveWord(content);
    }

    @Override
    public String filterContent(String content) {
        return sensitiveWordService.filterSensitiveWords(content);
    }

    private String generateExportContent(List<Article> articles, String format) {
        StringBuilder sb = new StringBuilder();

        if ("csv".equalsIgnoreCase(format)) {
            sb.append("ID,标题,摘要,状态,作者ID,分类ID,浏览量,点赞数,评论数,创建时间\n");
            for (Article article : articles) {
                sb.append(article.getId()).append(",")
                  .append(csvEscape(article.getTitle())).append(",")
                  .append(csvEscape(article.getSummary())).append(",")
                  .append(article.getStatus()).append(",")
                  .append(article.getAuthorId()).append(",")
                  .append(article.getCategoryId()).append(",")
                  .append(article.getViewCount()).append(",")
                  .append(article.getLikeCount()).append(",")
                  .append(article.getCommentCount()).append(",")
                  .append(article.getCreateTime()).append("\n");
            }
        } else {
            sb.append("[\n");
            for (int i = 0; i < articles.size(); i++) {
                Article article = articles.get(i);
                sb.append("  {\n");
                sb.append("    \"id\": ").append(article.getId()).append(",\n");
                sb.append("    \"title\": \"").append(jsonEscape(article.getTitle())).append("\",\n");
                sb.append("    \"summary\": \"").append(jsonEscape(article.getSummary())).append("\",\n");
                sb.append("    \"status\": \"").append(article.getStatus()).append("\",\n");
                sb.append("    \"authorId\": ").append(article.getAuthorId()).append(",\n");
                sb.append("    \"categoryId\": ").append(article.getCategoryId()).append(",\n");
                sb.append("    \"viewCount\": ").append(article.getViewCount()).append(",\n");
                sb.append("    \"likeCount\": ").append(article.getLikeCount()).append(",\n");
                sb.append("    \"commentCount\": ").append(article.getCommentCount()).append(",\n");
                sb.append("    \"createTime\": \"").append(article.getCreateTime()).append("\"\n");
                sb.append("  }");
                if (i < articles.size() - 1) {
                    sb.append(",");
                }
                sb.append("\n");
            }
            sb.append("]");
        }

        return sb.toString();
    }

    private String generateCommentExportContent(List<Comment> comments, String format) {
        StringBuilder sb = new StringBuilder();

        if ("csv".equalsIgnoreCase(format)) {
            sb.append("ID,内容,文章ID,用户ID,点赞数,状态,创建时间\n");
            for (Comment comment : comments) {
                sb.append(comment.getId()).append(",")
                  .append(csvEscape(comment.getContent())).append(",")
                  .append(comment.getArticleId()).append(",")
                  .append(comment.getUserId()).append(",")
                  .append(comment.getLikeCount()).append(",")
                  .append(comment.getStatus()).append(",")
                  .append(comment.getCreateTime()).append("\n");
            }
        } else {
            sb.append("[\n");
            for (int i = 0; i < comments.size(); i++) {
                Comment comment = comments.get(i);
                sb.append("  {\n");
                sb.append("    \"id\": ").append(comment.getId()).append(",\n");
                sb.append("    \"content\": \"").append(jsonEscape(comment.getContent())).append("\",\n");
                sb.append("    \"articleId\": ").append(comment.getArticleId()).append(",\n");
                sb.append("    \"userId\": ").append(comment.getUserId()).append(",\n");
                sb.append("    \"likeCount\": ").append(comment.getLikeCount()).append(",\n");
                sb.append("    \"status\": \"").append(comment.getStatus()).append("\",\n");
                sb.append("    \"createTime\": \"").append(comment.getCreateTime()).append("\"\n");
                sb.append("  }");
                if (i < comments.size() - 1) {
                    sb.append(",");
                }
                sb.append("\n");
            }
            sb.append("]");
        }

        return sb.toString();
    }

    private String csvEscape(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private String jsonEscape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
}