package com.example.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blog.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {

    List<Category> selectCategoriesWithCount();

    List<Category> selectRootCategories();

    List<Category> selectChildCategories(@Param("parentId") Long parentId);

    Category selectCategoryWithCount(@Param("id") Long id);

    boolean existsByName(@Param("name") String name);

    boolean existsByNameAndExcludeId(@Param("name") String name, @Param("excludeId") Long excludeId);

    int updateArticleCount(@Param("id") Long id, @Param("count") Integer count);

    int incrementArticleCount(@Param("id") Long id);

    int decrementArticleCount(@Param("id") Long id);

    List<Category> selectCategoriesBySortOrder();
}