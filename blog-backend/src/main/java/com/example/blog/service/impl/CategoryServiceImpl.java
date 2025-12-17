package com.example.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.blog.entity.Category;
import com.example.blog.mapper.CategoryMapper;
import com.example.blog.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 分类服务实现类
 */
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
    public Category createCategory(Category category) {
        // 检查分类名称是否已存在
        if (existsByName(category.getName())) {
            throw new RuntimeException("分类名称已存在");
        }

        // 设置默认值
        if (category.getSortOrder() == null) {
            category.setSortOrder(getMaxSortOrder(category.getParentId()) + 1);
        }
        if (category.getArticleCount() == null) {
            category.setArticleCount(0);
        }

        categoryMapper.insert(category);
        return category;
    }

    @Override
    @Transactional
    public Category updateCategory(Long id, Category category) {
        Category existingCategory = getCategoryById(id);
        if (existingCategory == null) {
            throw new RuntimeException("分类不存在");
        }

        // 检查分类名称是否已存在（排除当前分类）
        if (existsByNameAndExcludeId(category.getName(), id)) {
            throw new RuntimeException("分类名称已存在");
        }

        // 防止将分类设置为自己的子分类
        if (category.getParentId() != null && category.getParentId().equals(id)) {
            throw new RuntimeException("不能将分类设置为自己的子分类");
        }

        // 检查是否会形成循环引用
        if (category.getParentId() != null && wouldCreateCycle(id, category.getParentId())) {
            throw new RuntimeException("不能设置该父分类，会形成循环引用");
        }

        // 更新分类信息
        category.setId(id);
        categoryMapper.updateById(category);

        return getCategoryById(id);
    }

    @Override
    @Transactional
    public boolean deleteCategory(Long id) {
        Category category = getCategoryById(id);
        if (category == null) {
            return false;
        }

        // 检查是否有文章
        if (category.getArticleCount() > 0) {
            throw new RuntimeException("该分类下还有文章，不能删除");
        }

        // 将子分类的父ID设为null（变为顶级分类）
        UpdateWrapper<Category> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("parent_id", id)
                     .set("parent_id", null);
        categoryMapper.update(null, updateWrapper);

        // 删除分类
        return categoryMapper.deleteById(id) > 0;
    }

    @Override
    public Category getCategoryById(Long id) {
        return categoryMapper.selectCategoryWithCount(id);
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryMapper.selectCategoriesWithCount();
    }

    @Override
    public List<Category> getCategoryTree() {
        List<Category> allCategories = getAllCategories();
        return buildCategoryTree(allCategories);
    }

    @Override
    public List<Category> getRootCategories() {
        return categoryMapper.selectRootCategories();
    }

    @Override
    public List<Category> getChildCategories(Long parentId) {
        return categoryMapper.selectChildCategories(parentId);
    }

    @Override
    @Transactional
    public boolean updateCategorySort(List<Category> categories) {
        for (int i = 0; i < categories.size(); i++) {
            Category category = categories.get(i);
            UpdateWrapper<Category> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", category.getId())
                         .set("sort_order", i + 1)
                         .set("parent_id", category.getParentId());
            categoryMapper.update(null, updateWrapper);
        }
        return true;
    }

    @Override
    public boolean existsByName(String name) {
        return categoryMapper.existsByName(name);
    }

    @Override
    public boolean existsByNameAndExcludeId(String name, Long excludeId) {
        return categoryMapper.existsByNameAndExcludeId(name, excludeId);
    }

    @Override
    @Transactional
    public void incrementArticleCount(Long categoryId) {
        categoryMapper.incrementArticleCount(categoryId);
    }

    @Override
    @Transactional
    public void decrementArticleCount(Long categoryId) {
        categoryMapper.decrementArticleCount(categoryId);
    }

    /**
     * 构建分类树
     */
    private List<Category> buildCategoryTree(List<Category> categories) {
        // 按parent_id分组
        Map<Long, List<Category>> categoryMap = categories.stream()
                .filter(c -> c.getParentId() != null)
                .collect(Collectors.groupingBy(Category::getParentId));

        // 设置子分类
        categories.forEach(category -> {
            List<Category> children = categoryMap.get(category.getId());
            if (children != null) {
                children.sort((c1, c2) -> {
                    int sortCompare = Integer.compare(
                        c1.getSortOrder() != null ? c1.getSortOrder() : 0,
                        c2.getSortOrder() != null ? c2.getSortOrder() : 0
                    );
                    if (sortCompare != 0) return sortCompare;
                    return Long.compare(c1.getId(), c2.getId());
                });
                // 注意：Category实体没有children字段，这里需要在DTO中处理
            }
        });

        // 返回根分类
        return categories.stream()
                .filter(Category::isRootCategory)
                .sorted((c1, c2) -> {
                    int sortCompare = Integer.compare(
                        c1.getSortOrder() != null ? c1.getSortOrder() : 0,
                        c2.getSortOrder() != null ? c2.getSortOrder() : 0
                    );
                    if (sortCompare != 0) return sortCompare;
                    return Long.compare(c1.getId(), c2.getId());
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取指定父分类下的最大排序值
     */
    private Integer getMaxSortOrder(Long parentId) {
        QueryWrapper<Category> queryWrapper = new QueryWrapper<>();
        if (parentId == null) {
            queryWrapper.isNull("parent_id");
        } else {
            queryWrapper.eq("parent_id", parentId);
        }
        queryWrapper.select("MAX(sort_order) as sort_order");

        Category category = categoryMapper.selectOne(queryWrapper);
        return category != null && category.getSortOrder() != null ? category.getSortOrder() : 0;
    }

    /**
     * 检查是否会形成循环引用
     */
    private boolean wouldCreateCycle(Long categoryId, Long parentId) {
        Long currentId = parentId;
        int depth = 0;
        int maxDepth = 20; // 防止无限循环

        while (currentId != null && depth < maxDepth) {
            if (currentId.equals(categoryId)) {
                return true;
            }

            Category parent = categoryMapper.selectById(currentId);
            if (parent == null) {
                break;
            }
            currentId = parent.getParentId();
            depth++;
        }

        return false;
    }
}