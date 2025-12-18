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
        QueryWrapper<Tag> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", 0).orderByDesc("article_count").orderByAsc("id");
        return tagMapper.selectList(queryWrapper);
    }

    @Override
    public List<Tag> getPopularTags(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10;
        }
        QueryWrapper<Tag> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", 0).orderByDesc("article_count").orderByAsc("id").last("LIMIT " + limit);
        return tagMapper.selectList(queryWrapper);
    }

    @Override
    public List<Tag> getTagsByArticleId(Long articleId) {
        // 需要通过ArticleTag表关联查询，这里返回空列表简化实现
        QueryWrapper<Tag> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", 0);
        return tagMapper.selectList(queryWrapper);
    }

    @Override
    public boolean existsByName(String name) {
        QueryWrapper<Tag> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", 0).eq("name", name);
        return tagMapper.selectCount(queryWrapper) > 0;
    }

    @Override
    public boolean existsByNameAndExcludeId(String name, Long excludeId) {
        QueryWrapper<Tag> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", 0).eq("name", name).ne("id", excludeId);
        return tagMapper.selectCount(queryWrapper) > 0;
    }

    @Override
    @Transactional
    public void incrementArticleCount(Long tagId) {
        Tag tag = tagMapper.selectById(tagId);
        if (tag != null) {
            tag.setArticleCount((tag.getArticleCount() == null ? 0 : tag.getArticleCount()) + 1);
            tagMapper.updateById(tag);
        }
    }

    @Override
    @Transactional
    public void decrementArticleCount(Long tagId) {
        Tag tag = tagMapper.selectById(tagId);
        if (tag != null && tag.getArticleCount() != null && tag.getArticleCount() > 0) {
            tag.setArticleCount(tag.getArticleCount() - 1);
            tagMapper.updateById(tag);
        }
    }

    @Override
    public List<Tag> searchTagsByName(String name, Integer limit) {
        if (!StringUtils.hasText(name)) {
            return List.of();
        }
        if (limit == null || limit <= 0) {
            limit = 20;
        }
        QueryWrapper<Tag> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", 0).like("name", name).orderByDesc("article_count").orderByAsc("id");
        if (limit != null && limit > 0) {
            queryWrapper.last("LIMIT " + limit);
        }
        return tagMapper.selectList(queryWrapper);
    }
}