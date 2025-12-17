package com.example.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.blog.entity.Tag;
import com.example.blog.mapper.TagMapper;
import com.example.blog.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 标签服务实现类
 */
@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagMapper tagMapper;

    @Override
    @Transactional
    public Tag createTag(Tag tag) {
        // 检查标签名称是否已存在
        if (existsByName(tag.getName())) {
            throw new RuntimeException("标签名称已存在");
        }

        // 设置默认值
        if (tag.getArticleCount() == null) {
            tag.setArticleCount(0);
        }
        if (!StringUtils.hasText(tag.getColor())) {
            tag.setColor("#1890ff");
        }

        tagMapper.insert(tag);
        return tag;
    }

    @Override
    @Transactional
    public Tag updateTag(Long id, Tag tag) {
        Tag existingTag = getTagById(id);
        if (existingTag == null) {
            throw new RuntimeException("标签不存在");
        }

        // 检查标签名称是否已存在（排除当前标签）
        if (existsByNameAndExcludeId(tag.getName(), id)) {
            throw new RuntimeException("标签名称已存在");
        }

        // 更新标签信息
        tag.setId(id);
        tagMapper.updateById(tag);

        return getTagById(id);
    }

    @Override
    @Transactional
    public boolean deleteTag(Long id) {
        Tag tag = getTagById(id);
        if (tag == null) {
            return false;
        }

        // 删除标签（由于有外键级联删除，会自动删除文章标签关联）
        return tagMapper.deleteById(id) > 0;
    }

    @Override
    public Tag getTagById(Long id) {
        return tagMapper.selectById(id);
    }

    @Override
    public List<Tag> getAllTags() {
        return tagMapper.selectTagsWithCount();
    }

    @Override
    public List<Tag> getPopularTags(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10;
        }
        return tagMapper.selectPopularTags(limit);
    }

    @Override
    public List<Tag> getTagsByArticleId(Long articleId) {
        return tagMapper.selectTagsByArticleId(articleId);
    }

    @Override
    public boolean existsByName(String name) {
        return tagMapper.existsByName(name);
    }

    @Override
    public boolean existsByNameAndExcludeId(String name, Long excludeId) {
        return tagMapper.existsByNameAndExcludeId(name, excludeId);
    }

    @Override
    @Transactional
    public void incrementArticleCount(Long tagId) {
        tagMapper.incrementArticleCount(tagId);
    }

    @Override
    @Transactional
    public void decrementArticleCount(Long tagId) {
        tagMapper.decrementArticleCount(tagId);
    }

    @Override
    public List<Tag> searchTagsByName(String name, Integer limit) {
        if (!StringUtils.hasText(name)) {
            return List.of();
        }
        if (limit == null || limit <= 0) {
            limit = 20;
        }
        return tagMapper.selectTagsByNameLike(name, limit);
    }
}