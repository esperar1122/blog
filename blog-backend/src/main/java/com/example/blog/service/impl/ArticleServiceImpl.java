package com.example.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blog.entity.Article;
import com.example.blog.entity.ArticleLike;
import com.example.blog.entity.ArticleTag;
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
}