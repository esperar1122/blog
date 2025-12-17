package com.example.blog.dto;

import com.example.blog.entity.Category;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 分类数据传输对象
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class CategoryDTO extends Category {

    private List<CategoryDTO> children;

    /**
     * 转换为DTO（包含子分类）
     */
    public static CategoryDTO fromEntity(Category category, List<CategoryDTO> children) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setIcon(category.getIcon());
        dto.setParentId(category.getParentId());
        dto.setSortOrder(category.getSortOrder());
        dto.setArticleCount(category.getArticleCount());
        dto.setCreateTime(category.getCreateTime());
        dto.setUpdateTime(category.getUpdateTime());
        dto.setChildren(children);
        return dto;
    }

    /**
     * 转换为DTO（不包含子分类）
     */
    public static CategoryDTO fromEntity(Category category) {
        return fromEntity(category, null);
    }
}