package com.example.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blog.dto.request.ArticleQueryRequest;
import com.example.blog.dto.response.ArticleListResponse;
import com.example.blog.dto.response.ArticleDetailResponse;
import com.example.blog.entity.Article;
import com.example.blog.entity.ArticleLike;
import com.example.blog.entity.ArticleTag;
import com.example.blog.entity.Category;
import com.example.blog.entity.Tag;
import com.example.blog.entity.User;
import com.example.blog.exception.BusinessException;
import com.example.blog.mapper.ArticleLikeMapper;
import com.example.blog.mapper.ArticleMapper;
import com.example.blog.mapper.ArticleTagMapper;
import com.example.blog.mapper.CategoryMapper;
import com.example.blog.mapper.TagMapper;
import com.example.blog.mapper.UserMapper;
import com.example.blog.service.ArticleService;
import com.example.blog.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private final ArticleMapper articleMapper;
    private final ArticleTagMapper articleTagMapper;
    private final ArticleLikeMapper articleLikeMapper;
    private final UserMapper userMapper;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;
    private final NotificationService notificationService;

    @Override
    public Article getArticleById(Long id) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new BusinessException("文章不存在");
        }
        return article;
    }

    @Override
    public Article getArticleWithDetails(Long id) {
        Article article = articleMapper.selectArticleWithDetails(id);
        if (article == null) {
            throw new BusinessException("文章不存在");
        }
        return article;
    }

    @Override
    public IPage<Article> getArticlesWithPagination(Page<Article> page, Long categoryId, Long tagId, String keyword, String status) {
        return articleMapper.selectArticlesWithDetails(page, categoryId, tagId, keyword, status);
    }

    @Override
    public IPage<Article> getPublishedArticlesWithPagination(Page<Article> page, Long categoryId, Long tagId, String keyword) {
        return articleMapper.selectArticlesWithDetails(page, categoryId, tagId, keyword, Article.Status.PUBLISHED.getValue());
    }

    @Override
    @Transactional
    public Article createArticle(Article article, List<Long> tagIds) {
        article.setCreateTime(LocalDateTime.now());
        article.setUpdateTime(LocalDateTime.now());
        article.setViewCount(0);
        article.setLikeCount(0);
        article.setCommentCount(0);
        article.setIsTop(false);

        if (article.getStatus() == null) {
            article.setStatus(Article.Status.DRAFT.getValue());
        }

        articleMapper.insert(article);

        if (tagIds != null && !tagIds.isEmpty()) {
            saveArticleTags(article.getId(), tagIds);
            updateTagsArticleCount(tagIds, 1);
        }

        if (article.getCategoryId() != null) {
            categoryMapper.incrementArticleCount(article.getCategoryId());
        }

        return article;
    }

    @Override
    @Transactional
    public Article updateArticle(Long id, Article article, List<Long> tagIds, Long currentUserId) {
        Article existingArticle = getArticleById(id);

        if (!existingArticle.getAuthorId().equals(currentUserId)) {
            throw new BusinessException("无权限修改此文章");
        }

        existingArticle.setTitle(article.getTitle());
        existingArticle.setContent(article.getContent());
        existingArticle.setSummary(article.getSummary());
        existingArticle.setCoverImage(article.getCoverImage());
        existingArticle.setUpdateTime(LocalDateTime.now());

        if (article.getCategoryId() != null && !article.getCategoryId().equals(existingArticle.getCategoryId())) {
            if (existingArticle.getCategoryId() != null) {
                categoryMapper.decrementArticleCount(existingArticle.getCategoryId());
            }
            categoryMapper.incrementArticleCount(article.getCategoryId());
            existingArticle.setCategoryId(article.getCategoryId());
        }

        articleMapper.updateById(existingArticle);

        if (tagIds != null) {
            List<Long> oldTagIds = articleTagMapper.selectTagIdsByArticleId(id);

            articleTagMapper.deleteArticleTagsByArticleId(id);
            saveArticleTags(id, tagIds);

            updateTagsArticleCount(oldTagIds, -1);
            updateTagsArticleCount(tagIds, 1);
        }

        return existingArticle;
    }

    @Override
    @Transactional
    public boolean deleteArticle(Long id, Long currentUserId) {
        Article article = getArticleById(id);

        if (!article.getAuthorId().equals(currentUserId)) {
            throw new BusinessException("无权限删除此文章");
        }

        article.setStatus(Article.Status.DELETED.getValue());
        article.setUpdateTime(LocalDateTime.now());

        boolean result = articleMapper.updateById(article) > 0;

        if (result) {
            articleTagMapper.deleteArticleTagsByArticleId(id);
            articleLikeMapper.deleteLikesByArticleId(id);

            if (article.getCategoryId() != null) {
                categoryMapper.decrementArticleCount(article.getCategoryId());
            }

            List<Long> tagIds = articleTagMapper.selectTagIdsByArticleId(id);
            updateTagsArticleCount(tagIds, -1);
        }

        return result;
    }

    @Override
    @Transactional
    public boolean publishArticle(Long id, Long currentUserId) {
        Article article = getArticleById(id);

        if (!article.getAuthorId().equals(currentUserId)) {
            throw new BusinessException("无权限发布此文章");
        }

        article.setStatus(Article.Status.PUBLISHED.getValue());
        article.setPublishTime(LocalDateTime.now());
        article.setUpdateTime(LocalDateTime.now());

        return articleMapper.updateById(article) > 0;
    }

    @Override
    @Transactional
    public boolean unpublishArticle(Long id, Long currentUserId) {
        Article article = getArticleById(id);

        if (!article.getAuthorId().equals(currentUserId)) {
            throw new BusinessException("无权限取消发布此文章");
        }

        article.setStatus(Article.Status.DRAFT.getValue());
        article.setUpdateTime(LocalDateTime.now());

        return articleMapper.updateById(article) > 0;
    }

    @Override
    @Transactional
    public boolean incrementViewCount(Long id) {
        return articleMapper.incrementViewCount(id) > 0;
    }

    @Override
    @Transactional
    public boolean likeArticle(Long id, Long userId) {
        if (articleLikeMapper.existsByArticleIdAndUserId(id, userId)) {
            return false;
        }

        ArticleLike like = new ArticleLike();
        like.setArticleId(id);
        like.setUserId(userId);
        like.setCreateTime(LocalDateTime.now());

        articleLikeMapper.insert(like);
        articleMapper.incrementLikeCount(id);

        Article article = getArticleById(id);
        if (!article.getAuthorId().equals(userId)) {
            User liker = userMapper.selectById(userId);
            String title = "文章收到点赞";
            String content = String.format("用户 %s 点赞了您的文章《%s》", liker.getNickname(), article.getTitle());
            notificationService.createLikeNotification(article.getAuthorId(), title, content, id);
        }

        return true;
    }

    @Override
    @Transactional
    public boolean unlikeArticle(Long id, Long userId) {
        if (!articleLikeMapper.existsByArticleIdAndUserId(id, userId)) {
            return false;
        }

        articleLikeMapper.deleteByArticleIdAndUserId(id, userId);
        articleMapper.decrementLikeCount(id);

        return true;
    }

    @Override
    @Transactional
    public boolean toggleLike(Long id, Long userId) {
        if (articleLikeMapper.existsByArticleIdAndUserId(id, userId)) {
            return unlikeArticle(id, userId);
        } else {
            return likeArticle(id, userId);
        }
    }

    @Override
    public List<Article> getArticlesByAuthor(Long authorId, String status, Integer limit) {
        return articleMapper.selectArticlesByAuthor(authorId, status, limit);
    }

    @Override
    public List<Article> getPopularArticles(Integer limit) {
        return articleMapper.selectPopularArticles(limit);
    }

    @Override
    public List<Article> getLatestArticles(Integer limit) {
        return articleMapper.selectLatestArticles(limit);
    }

    @Override
    public List<Article> getRelatedArticles(Long id, Integer limit) {
        Article article = getArticleById(id);
        return articleMapper.selectRelatedArticles(id, article.getCategoryId(), limit);
    }

    @Override
    public List<Article> getDraftArticles(Long authorId) {
        return articleMapper.selectDraftArticles(authorId);
    }

    @Override
    public List<Article> getPublishedArticles(Long authorId) {
        return articleMapper.selectPublishedArticles(authorId);
    }

    @Override
    @Transactional
    public boolean setArticleTop(Long id, boolean isTop, Long currentUserId) {
        Article article = getArticleById(id);
        User user = userMapper.selectById(currentUserId);

        if (!article.getAuthorId().equals(currentUserId) && !user.isAdmin()) {
            throw new BusinessException("无权限置顶此文章");
        }

        article.setIsTop(isTop);
        article.setUpdateTime(LocalDateTime.now());

        return articleMapper.updateById(article) > 0;
    }

    @Override
    public List<Article> searchArticles(String keyword, Integer limit) {
        QueryWrapper<Article> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", Article.Status.PUBLISHED.getValue())
                   .and(wrapper -> wrapper.like("title", keyword)
                                          .or()
                                          .like("content", keyword))
                   .orderByDesc("view_count")
                   .last("LIMIT " + (limit != null ? limit : 20));

        return articleMapper.selectList(queryWrapper);
    }

    @Override
    public int countArticlesByAuthor(Long authorId, String status) {
        QueryWrapper<Article> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("author_id", authorId);
        if (status != null) {
            queryWrapper.eq("status", status);
        }
        return Math.toIntExact(articleMapper.selectCount(queryWrapper));
    }

    @Override
    public int countAllArticles(String status) {
        QueryWrapper<Article> queryWrapper = new QueryWrapper<>();
        if (status != null) {
            queryWrapper.eq("status", status);
        }
        return Math.toIntExact(articleMapper.selectCount(queryWrapper));
    }

    private void saveArticleTags(Long articleId, List<Long> tagIds) {
        for (Long tagId : tagIds) {
            ArticleTag articleTag = new ArticleTag(articleId, tagId);
            articleTagMapper.insert(articleTag);
        }
    }

    private void updateTagsArticleCount(List<Long> tagIds, int increment) {
        if (tagIds != null && !tagIds.isEmpty()) {
            for (Long tagId : tagIds) {
                if (increment > 0) {
                    tagMapper.incrementArticleCount(tagId);
                } else {
                    tagMapper.decrementArticleCount(tagId);
                }
            }
        }
    }

    @Override
    public int getTotalArticleCount() {
        return articleMapper.selectCount(null);
    }

    @Override
    public int getPublishedArticleCount() {
        QueryWrapper<Article> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", "PUBLISHED");
        return articleMapper.selectCount(queryWrapper);
    }

    @Override
    public ArticleListResponse getArticleList(ArticleQueryRequest request) {
        Page<Article> page = new Page<>(request.getPage(), request.getSize());
        IPage<Article> articlePage = articleMapper.selectArticlesWithAdvancedQuery(page, request);

        ArticleListResponse response = new ArticleListResponse();
        response.setContent(convertToArticleSummaries(articlePage.getRecords()));
        response.setTotalPages((int) articlePage.getPages());
        response.setTotalElements(articlePage.getTotal());
        response.setSize((int) articlePage.getSize());
        response.setNumber((int) articlePage.getCurrent());
        response.setFirst(articlePage.getCurrent() == 1);
        response.setLast(articlePage.getCurrent() == articlePage.getPages());
        response.setEmpty(articlePage.getRecords().isEmpty());

        return response;
    }

    @Override
    public ArticleDetailResponse getArticleDetail(Long id, Long currentUserId) {
        Article article = getArticleWithDetails(id);

        // 检查权限
        if (article.isDraft() && !article.getAuthorId().equals(currentUserId)) {
            throw new BusinessException("无权限访问此文章");
        }

        // 增加阅读量
        incrementViewCount(id);

        ArticleDetailResponse response = new ArticleDetailResponse();
        response.setArticle(article);
        response.setAuthor(convertToUserSummary(article));
        response.setCategory(convertToCategorySummary(article));
        response.setTags(convertToTagSummaries(article.getTags()));
        response.setLiked(currentUserId != null && articleLikeMapper.existsByArticleIdAndUserId(id, currentUserId));
        response.setCanEdit(article.getAuthorId().equals(currentUserId));

        return response;
    }

    @Override
    public ArticleListResponse searchArticles(ArticleQueryRequest request) {
        Page<Article> page = new Page<>(request.getPage(), request.getSize());

        // 如果是搜索请求，使用关键词搜索
        if (request.getKeyword() != null && !request.getKeyword().trim().isEmpty()) {
            List<Article> searchResults = articleMapper.selectArticlesByKeyword(request.getKeyword(), request.getSize());
            ArticleListResponse response = new ArticleListResponse();
            response.setContent(convertToArticleSummaries(searchResults));
            response.setTotalPages(1);
            response.setTotalElements(searchResults.size());
            response.setSize(searchResults.size());
            response.setNumber(0);
            response.setFirst(true);
            response.setLast(true);
            response.setEmpty(searchResults.isEmpty());
            return response;
        } else {
            // 否则使用高级查询
            return getArticleList(request);
        }
    }

    private List<ArticleListResponse.ArticleSummary> convertToArticleSummaries(List<Article> articles) {
        return articles.stream()
                .collect(Collectors.groupingBy(Article::getId))
                .values()
                .stream()
                .map(articleList -> {
                    Article article = articleList.get(0);
                    ArticleListResponse.ArticleSummary summary = new ArticleListResponse.ArticleSummary();

                    summary.setId(article.getId());
                    summary.setTitle(article.getTitle());
                    summary.setSummary(article.getSummary());
                    summary.setCoverImage(article.getCoverImage());
                    summary.setStatus(article.getStatus());
                    summary.setViewCount(article.getViewCount());
                    summary.setLikeCount(article.getLikeCount());
                    summary.setCommentCount(article.getCommentCount());
                    summary.setIsTop(article.getIsTop());
                    summary.setAuthorId(article.getAuthorId());
                    summary.setAuthorName(article.getAuthorName() != null ? article.getAuthorName() : "");
                    summary.setAuthorAvatar(article.getAuthorAvatar() != null ? article.getAuthorAvatar() : "");
                    summary.setCategoryId(article.getCategoryId());
                    summary.setCategoryName(article.getCategoryName() != null ? article.getCategoryName() : "");
                    summary.setCreateTime(article.getCreateTime());
                    summary.setUpdateTime(article.getUpdateTime());
                    summary.setPublishTime(article.getPublishTime());

                    // 处理标签
                    List<String> tagNames = articleList.stream()
                            .flatMap(a -> a.getTags() != null ? a.getTags().stream() : Stream.empty())
                            .map(Tag::getName)
                            .distinct()
                            .collect(Collectors.toList());
                    summary.setTags(tagNames);

                    return summary;
                })
                .collect(Collectors.toList());
    }

    private ArticleDetailResponse.UserSummary convertToUserSummary(Article article) {
        ArticleDetailResponse.UserSummary userSummary = new ArticleDetailResponse.UserSummary();
        userSummary.setId(article.getAuthorId());
        userSummary.setUsername(article.getAuthorName() != null ? article.getAuthorName() : "");
        userSummary.setNickname(article.getAuthorName() != null ? article.getAuthorName() : "");
        userSummary.setAvatar(article.getAuthorAvatar() != null ? article.getAuthorAvatar() : "");
        return userSummary;
    }

    private ArticleDetailResponse.CategorySummary convertToCategorySummary(Article article) {
        ArticleDetailResponse.CategorySummary categorySummary = new ArticleDetailResponse.CategorySummary();
        if (article.getCategoryId() != null) {
            categorySummary.setId(article.getCategoryId());
            categorySummary.setName(article.getCategoryName() != null ? article.getCategoryName() : "");
        }
        return categorySummary;
    }

    private List<ArticleDetailResponse.TagSummary> convertToTagSummaries(List<Tag> tags) {
        if (tags == null || tags.isEmpty()) {
            return new ArrayList<>();
        }
        return tags.stream()
                .map(tag -> {
                    ArticleDetailResponse.TagSummary tagSummary = new ArticleDetailResponse.TagSummary();
                    tagSummary.setId(tag.getId());
                    tagSummary.setName(tag.getName());
                    tagSummary.setColor(tag.getColor() != null ? tag.getColor() : "");
                    return tagSummary;
                })
                .collect(Collectors.toList());
    }
}