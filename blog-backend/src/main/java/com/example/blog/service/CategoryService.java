package com.example.blog.service;

import com.example.blog.entity.Category;
import java.util.List;

/**
 * 分类服务接口
 */
public interface CategoryService {

    /**
     * 创建分类
     */
    Category createCategory(Category category);

    /**
     * 更新分类
     */
    Category updateCategory(Long id, Category category);

    /**
     * 删除分类
     */
    boolean deleteCategory(Long id);

    /**
     * 获取分类详情
     */
    Category getCategoryById(Long id);

    /**
     * 获取所有分类列表
     */
    List<Category> getAllCategories();

    /**
     * 获取分类树形结构
     */
    List<Category> getCategoryTree();

    /**
     * 获取根分类列表
     */
    List<Category> getRootCategories();

    /**
     * 获取子分类列表
     */
    List<Category> getChildCategories(Long parentId);

    /**
     * 更新分类排序
     */
    boolean updateCategorySort(List<Category> categories);

    /**
     * 检查分类名称是否存在
     */
    boolean existsByName(String name);

    /**
     * 检查分类名称是否存在（排除指定ID）
     */
    boolean existsByNameAndExcludeId(String name, Long excludeId);

    /**
     * 增加文章数量
     */
    void incrementArticleCount(Long categoryId);

    /**
     * 减少文章数量
     */
    void decrementArticleCount(Long categoryId);
}