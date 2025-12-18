package com.example.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blog.dto.request.ArticleQueryRequest;
import com.example.blog.dto.response.ArticleListResponse;
import com.example.blog.dto.response.ArticleDetailResponse;
import com.example.blog.dto.CreateArticleRequest;
import com.example.blog.dto.UpdateArticleRequest;
import com.example.blog.entity.Article;
import com.example.blog.entity.ArticleLike;
import com.example.blog.entity.ArticleTag;
import com.example.blog.entity.Category;
import com.example.blog.entity.Tag;
import com.example.blog.entity.User;
import com.example.blog.enums.ArticleStatus;
import com.example.blog.exception.BusinessException;
import com.example.blog.mapper.ArticleLikeMapper;
import com.example.blog.mapper.ArticleMapper;
import com.example.blog.mapper.ArticleTagMapper;
import com.example.blog.mapper.CategoryMapper;
import com.example.blog.mapper.TagMapper;
import com.example.blog.mapper.UserMapper;
import com.example.blog.service.ArticleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private final ArticleMapper articleMapper;
    private final ArticleTagMapper articleTagMapper;
    private final ArticleLikeMapper articleLikeMapper;
    private final UserMapper userMapper;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;

    @Override
    public Article getArticleById(Long id) {
        Article article = articleMapper.selectById(id);
        if (article == null || article.getDeleted() == 1) {
            throw new BusinessException("文章不存在");
        }
        return article;
    }

    @Override
    public ArticleListResponse getArticleList(ArticleQueryRequest request) {
        // 构建查询条件
        QueryWrapper<Article> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", 0);

        // 状态过滤
        if (request.getStatus() != null && !request.getStatus().isEmpty()) {
            queryWrapper.eq("status", request.getStatus());
        }

        // 分类过滤
        if (request.getCategoryId() != null) {
            queryWrapper.eq("category_id", request.getCategoryId());
        }

        // 作者过滤
        if (request.getAuthorId() != null) {
            queryWrapper.eq("author_id", request.getAuthorId());
        }

        // 关键词搜索
        if (request.getKeyword() != null && !request.getKeyword().trim().isEmpty()) {
            String keyword = request.getKeyword().trim();
            queryWrapper.and(wrapper -> wrapper
                .like("title", keyword)
                .or()
                .like("content", keyword)
                .or()
                .like("summary", keyword)
            );
        }

        // 标签过滤
        if (request.getTagId() != null) {
            // 需要通过 article_tag 表关联查询
            List<Long> articleIds = getArticleIdsByTagId(request.getTagId());
            if (articleIds.isEmpty()) {
                // 没有匹配的文章
                ArticleListResponse response = new ArticleListResponse();
                response.setArticles(new ArrayList<>());
                response.setTotal(0L);
                response.setCurrent((long) request.getPage());
                response.setSize((long) request.getSize());
                response.setPages(0L);
                return response;
            }
            queryWrapper.in("id", articleIds);
        }

        // 排序
        if ("views".equals(request.getSort())) {
            queryWrapper.orderByDesc("view_count");
        } else if ("likes".equals(request.getSort())) {
            queryWrapper.orderByDesc("like_count");
        } else {
            queryWrapper.orderByDesc("create_time");
        }

        // 分页查询
        Page<Article> pageParam = new Page<>(request.getPage(), request.getSize());
        IPage<Article> pageResult = articleMapper.selectPage(pageParam, queryWrapper);

        // 转换为响应对象
        List<ArticleDetailResponse> articleResponses = pageResult.getRecords().stream()
            .map(this::convertToDetailResponse)
            .collect(Collectors.toList());

        // 构建响应
        ArticleListResponse response = new ArticleListResponse();
        response.setArticles(articleResponses);
        response.setTotal(pageResult.getTotal());
        response.setCurrent(pageResult.getCurrent());
        response.setSize(pageResult.getSize());
        response.setPages(pageResult.getPages());

        return response;
    }

    @Override
    @Transactional
    public Article createArticle(CreateArticleRequest request) {
        // 验证分类是否存在
        if (request.getCategoryId() != null) {
            Category category = categoryMapper.selectById(request.getCategoryId());
            if (category == null || category.getDeleted() == 1) {
                throw new BusinessException("分类不存在");
            }
        }

        // 验证标签是否存在
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            List<Tag> tags = tagMapper.selectBatchIds(request.getTagIds());
            if (tags.size() != request.getTagIds().size()) {
                throw new BusinessException("部分标签不存在");
            }
        }

        // 创建文章
        Article article = new Article();
        article.setTitle(request.getTitle());
        article.setContent(request.getContent());
        article.setSummary(request.getSummary());
        article.setAuthorId(1L); // 默认作者ID，实际应从当前用户获取
        article.setCategoryId(request.getCategoryId());
        article.setStatus(ArticleStatus.DRAFT.toString());
        article.setViewCount(0L);
        article.setLikeCount(0L);
        article.setCommentCount(0L);
        article.setCreateTime(LocalDateTime.now());
        article.setUpdateTime(LocalDateTime.now());

        // 插入文章
        articleMapper.insert(article);

        // 关联标签
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            for (Long tagId : request.getTagIds()) {
                ArticleTag articleTag = new ArticleTag();
                articleTag.setArticleId(article.getId());
                articleTag.setTagId(tagId);
                articleTag.setCreateTime(LocalDateTime.now());
                articleTagMapper.insert(articleTag);
            }
        }

        log.info("创建文章成功 - id: {}, title: {}", article.getId(), article.getTitle());
        return article;
    }

    @Override
    @Transactional
    public Article updateArticle(Long id, UpdateArticleRequest request) {
        Article article = getArticleById(id);

        // 验证分类是否存在
        if (request.getCategoryId() != null && !request.getCategoryId().equals(article.getCategoryId())) {
            if (request.getCategoryId() != null) {
                Category category = categoryMapper.selectById(request.getCategoryId());
                if (category == null || category.getDeleted() == 1) {
                    throw new BusinessException("分类不存在");
                }
            }
            article.setCategoryId(request.getCategoryId());
        }

        // 更新基本信息
        if (request.getTitle() != null) {
            article.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            article.setContent(request.getContent());
        }
        if (request.getSummary() != null) {
            article.setSummary(request.getSummary());
        }

        article.setUpdateTime(LocalDateTime.now());

        // 更新文章
        articleMapper.updateById(article);

        // 更新标签关联
        if (request.getTagIds() != null) {
            // 删除原有标签关联
            QueryWrapper<ArticleTag> deleteWrapper = new QueryWrapper<>();
            deleteWrapper.eq("article_id", id);
            articleTagMapper.delete(deleteWrapper);

            // 添加新的标签关联
            if (!request.getTagIds().isEmpty()) {
                // 验证标签是否存在
                List<Tag> tags = tagMapper.selectBatchIds(request.getTagIds());
                if (tags.size() != request.getTagIds().size()) {
                    throw new BusinessException("部分标签不存在");
                }

                for (Long tagId : request.getTagIds()) {
                    ArticleTag articleTag = new ArticleTag();
                    articleTag.setArticleId(article.getId());
                    articleTag.setTagId(tagId);
                    articleTag.setCreateTime(LocalDateTime.now());
                    articleTagMapper.insert(articleTag);
                }
            }
        }

        log.info("更新文章成功 - id: {}, title: {}", article.getId(), article.getTitle());
        return article;
    }

    @Override
    @Transactional
    public boolean deleteArticle(Long id) {
        Article article = getArticleById(id);

        // 软删除：设置为已删除状态
        article.setDeleted(1);
        article.setUpdateTime(LocalDateTime.now());

        int result = articleMapper.updateById(article);

        if (result > 0) {
            // 删除标签关联
            QueryWrapper<ArticleTag> deleteWrapper = new QueryWrapper<>();
            deleteWrapper.eq("article_id", id);
            articleTagMapper.delete(deleteWrapper);

            log.info("删除文章成功 - id: {}, title: {}", article.getId(), article.getTitle());
        }

        return result > 0;
    }

    @Override
    public boolean publishArticle(Long id) {
        Article article = getArticleById(id);

        article.setStatus(ArticleStatus.PUBLISHED.toString());
        article.setUpdateTime(LocalDateTime.now());

        int result = articleMapper.updateById(article);

        if (result > 0) {
            log.info("发布文章成功 - id: {}, title: {}", article.getId(), article.getTitle());
        }

        return result > 0;
    }

    @Override
    public boolean unpublishArticle(Long id) {
        Article article = getArticleById(id);

        article.setStatus(ArticleStatus.DRAFT.toString());
        article.setUpdateTime(LocalDateTime.now());

        int result = articleMapper.updateById(article);

        if (result > 0) {
            log.info("取消发布文章成功 - id: {}, title: {}", article.getId(), article.getTitle());
        }

        return result > 0;
    }

    @Override
    public ArticleDetailResponse getArticleDetail(Long id) {
        Article article = getArticleById(id);

        // 增加浏览次数
        article.setViewCount(article.getViewCount() + 1);
        articleMapper.updateById(article);

        return convertToDetailResponse(article);
    }

    @Override
    @Transactional
    public boolean likeArticle(Long articleId, Long userId) {
        Article article = getArticleById(articleId);

        // 检查是否已点赞
        QueryWrapper<ArticleLike> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("article_id", articleId).eq("user_id", userId);
        ArticleLike existingLike = articleLikeMapper.selectOne(queryWrapper);

        if (existingLike != null) {
            // 取消点赞
            articleLikeMapper.deleteById(existingLike.getId());
            article.setLikeCount(Math.max(0, article.getLikeCount() - 1));
        } else {
            // 点赞
            ArticleLike articleLike = new ArticleLike();
            articleLike.setArticleId(articleId);
            articleLike.setUserId(userId);
            articleLike.setCreatedAt(LocalDateTime.now());
            articleLikeMapper.insert(articleLike);
            article.setLikeCount(article.getLikeCount() + 1);
        }

        article.setUpdateTime(LocalDateTime.now());
        articleMapper.updateById(article);

        return existingLike == null; // 返回是否是点赞操作
    }

    @Override
    public Map<String, Object> getArticleStatistics() {
        Map<String, Object> statistics = new HashMap<>();

        QueryWrapper<Article> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", 0);

        Long totalArticles = articleMapper.selectCount(queryWrapper);
        statistics.put("totalArticles", totalArticles);

        // 按状态统计
        QueryWrapper<Article> publishedWrapper = new QueryWrapper<>();
        publishedWrapper.eq("deleted", 0).eq("status", "PUBLISHED");
        Long publishedCount = articleMapper.selectCount(publishedWrapper);
        statistics.put("publishedCount", publishedCount);

        QueryWrapper<Article> draftWrapper = new QueryWrapper<>();
        draftWrapper.eq("deleted", 0).eq("status", "DRAFT");
        Long draftCount = articleMapper.selectCount(draftWrapper);
        statistics.put("draftCount", draftCount);

        // 最近创建文章数量（最近7天）
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        QueryWrapper<Article> recentWrapper = new QueryWrapper<>();
        recentWrapper.eq("deleted", 0)
                    .ge("create_time", sevenDaysAgo);
        Long recentArticleCount = articleMapper.selectCount(recentWrapper);
        statistics.put("recentArticleCount", recentArticleCount);

        return statistics;
    }

    private ArticleDetailResponse convertToDetailResponse(Article article) {
        ArticleDetailResponse response = new ArticleDetailResponse();
        response.setId(article.getId());
        response.setTitle(article.getTitle());
        response.setContent(article.getContent());
        response.setSummary(article.getSummary());
        response.setAuthorId(article.getAuthorId());
        response.setCategoryId(article.getCategoryId());
        response.setStatus(article.getStatus());
        response.setViewCount(article.getViewCount());
        response.setLikeCount(article.getLikeCount());
        response.setCommentCount(article.getCommentCount());
        response.setCreatedAt(article.getCreatedAt());
        response.setUpdatedAt(article.getUpdatedAt());

        // 获取作者信息
        if (article.getAuthorId() != null) {
            User author = userMapper.selectById(article.getAuthorId());
            if (author != null && author.getDeleted() == 0) {
                response.setAuthorName(author.getUsername());
                response.setAuthorAvatar(author.getAvatar());
            }
        }

        // 获取分类信息
        if (article.getCategoryId() != null) {
            Category category = categoryMapper.selectById(article.getCategoryId());
            if (category != null && category.getDeleted() == 0) {
                response.setCategoryName(category.getName());
            }
        }

        // 获取标签信息
        List<Tag> tags = getTagsByArticleId(article.getId());
        response.setTags(tags);

        return response;
    }

    private List<Long> getArticleIdsByTagId(Long tagId) {
        QueryWrapper<ArticleTag> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("tag_id", tagId);
        List<ArticleTag> articleTags = articleTagMapper.selectList(queryWrapper);
        return articleTags.stream()
                .map(ArticleTag::getArticleId)
                .collect(Collectors.toList());
    }

    private List<Tag> getTagsByArticleId(Long articleId) {
        QueryWrapper<ArticleTag> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("article_id", articleId);
        List<ArticleTag> articleTags = articleTagMapper.selectList(queryWrapper);

        if (articleTags.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> tagIds = articleTags.stream()
                .map(ArticleTag::getTagId)
                .collect(Collectors.toList());

        return tagMapper.selectBatchIds(tagIds);
    }
}