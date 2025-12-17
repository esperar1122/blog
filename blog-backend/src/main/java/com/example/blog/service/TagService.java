package com.example.blog.service;

import com.example.blog.entity.Tag;
import java.util.List;

/**
 * 标签服务接口
 */
public interface TagService {

    /**
     * 创建标签
     */
    Tag createTag(Tag tag);

    /**
     * 更新标签
     */
    Tag updateTag(Long id, Tag tag);

    /**
     * 删除标签
     */
    boolean deleteTag(Long id);

    /**
     * 获取标签详情
     */
    Tag getTagById(Long id);

    /**
     * 获取所有标签列表
     */
    List<Tag> getAllTags();

    /**
     * 获取热门标签
     */
    List<Tag> getPopularTags(Integer limit);

    /**
     * 根据文章ID获取标签列表
     */
    List<Tag> getTagsByArticleId(Long articleId);

    /**
     * 检查标签名称是否存在
     */
    boolean existsByName(String name);

    /**
     * 检查标签名称是否存在（排除指定ID）
     */
    boolean existsByNameAndExcludeId(String name, Long excludeId);

    /**
     * 增加文章数量
     */
    void incrementArticleCount(Long tagId);

    /**
     * 减少文章数量
     */
    void decrementArticleCount(Long tagId);

    /**
     * 根据名称搜索标签
     */
    List<Tag> searchTagsByName(String name, Integer limit);
}