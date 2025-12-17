package com.example.blog.service;

import com.example.blog.entity.Tag;
import com.example.blog.exception.BusinessException;
import com.example.blog.mapper.TagMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TagService 单元测试")
class TagServiceTest {

    @Mock
    private TagMapper tagMapper;

    @InjectMocks
    private TagServiceImpl tagService;

    private Tag testTag;

    @BeforeEach
    void setUp() {
        testTag = new Tag();
        testTag.setId(1L);
        testTag.setName("Java");
        testTag.setColor("#007396");
        testTag.setArticleCount(5);
    }

    @Test
    @DisplayName("创建标签 - 成功")
    void createTag_Success() {
        // Given
        Tag newTag = new Tag();
        newTag.setName("Spring Boot");
        newTag.setColor("#6DB33F");

        when(tagMapper.existsByName("Spring Boot")).thenReturn(false);
        when(tagMapper.insert(any(Tag.class))).thenReturn(1);
        when(tagMapper.selectById(any())).thenReturn(testTag);

        // When
        Tag result = tagService.createTag(newTag);

        // Then
        assertNotNull(result);
        verify(tagMapper).existsByName("Spring Boot");
        verify(tagMapper).insert(newTag);
    }

    @Test
    @DisplayName("创建标签 - 名称已存在")
    void createTag_NameExists() {
        // Given
        Tag newTag = new Tag();
        newTag.setName("Java");

        when(tagMapper.existsByName("Java")).thenReturn(true);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            tagService.createTag(newTag);
        });

        assertEquals("标签名称已存在", exception.getMessage());
        verify(tagMapper, never()).insert(any());
    }

    @Test
    @DisplayName("获取所有标签 - 成功")
    void getAllTags_Success() {
        // Given
        List<Tag> tags = Arrays.asList(testTag);
        when(tagMapper.selectTagsWithCount()).thenReturn(tags);

        // When
        List<Tag> result = tagService.getAllTags();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Java", result.get(0).getName());
        verify(tagMapper).selectTagsWithCount();
    }

    @Test
    @DisplayName("根据ID获取标签 - 成功")
    void getTagById_Success() {
        // Given
        when(tagMapper.selectById(1L)).thenReturn(testTag);

        // When
        Tag result = tagService.getTagById(1L);

        // Then
        assertNotNull(result);
        assertEquals("Java", result.getName());
        verify(tagMapper).selectById(1L);
    }

    @Test
    @DisplayName("根据ID获取标签 - 不存在")
    void getTagById_NotFound() {
        // Given
        when(tagMapper.selectById(999L)).thenReturn(null);

        // When
        Tag result = tagService.getTagById(999L);

        // Then
        assertNull(result);
        verify(tagMapper).selectById(999L);
    }

    @Test
    @DisplayName("获取热门标签 - 成功")
    void getPopularTags_Success() {
        // Given
        List<Tag> popularTags = Arrays.asList(testTag);
        when(tagMapper.selectPopularTags(10)).thenReturn(popularTags);

        // When
        List<Tag> result = tagService.getPopularTags(10);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(tagMapper).selectPopularTags(10);
    }

    @Test
    @DisplayName("更新标签 - 成功")
    void updateTag_Success() {
        // Given
        Tag updateTag = new Tag();
        updateTag.setName("Spring Framework");
        updateTag.setColor("#6DB33F");

        when(tagMapper.selectById(1L)).thenReturn(testTag);
        when(tagMapper.existsByNameAndExcludeId("Spring Framework", 1L)).thenReturn(false);
        when(tagMapper.updateById(any(Tag.class))).thenReturn(1);

        // When
        Tag result = tagService.updateTag(1L, updateTag);

        // Then
        assertNotNull(result);
        verify(tagMapper).selectById(1L);
        verify(tagMapper).existsByNameAndExcludeId("Spring Framework", 1L);
        verify(tagMapper).updateById(any(Tag.class));
    }

    @Test
    @DisplayName("更新标签 - 标签不存在")
    void updateTag_TagNotFound() {
        // Given
        when(tagMapper.selectById(999L)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            tagService.updateTag(999L, new Tag());
        });

        assertEquals("标签不存在", exception.getMessage());
    }

    @Test
    @DisplayName("更新标签 - 名称已存在")
    void updateTag_NameExists() {
        // Given
        Tag updateTag = new Tag();
        updateTag.setName("Existing Tag");

        when(tagMapper.selectById(1L)).thenReturn(testTag);
        when(tagMapper.existsByNameAndExcludeId("Existing Tag", 1L)).thenReturn(true);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            tagService.updateTag(1L, updateTag);
        });

        assertEquals("标签名称已存在", exception.getMessage());
        verify(tagMapper, never()).updateById(any());
    }

    @Test
    @DisplayName("删除标签 - 成功")
    void deleteTag_Success() {
        // Given
        when(tagMapper.selectById(1L)).thenReturn(testTag);
        when(tagMapper.deleteById(1L)).thenReturn(1);

        // When
        boolean result = tagService.deleteTag(1L);

        // Then
        assertTrue(result);
        verify(tagMapper).selectById(1L);
        verify(tagMapper).deleteById(1L);
    }

    @Test
    @DisplayName("删除标签 - 标签不存在")
    void deleteTag_NotFound() {
        // Given
        when(tagMapper.selectById(999L)).thenReturn(null);

        // When
        boolean result = tagService.deleteTag(999L);

        // Then
        assertFalse(result);
        verify(tagMapper, never()).deleteById(any());
    }

    @Test
    @DisplayName("删除标签 - 有关联文章")
    void deleteTag_HasArticles() {
        // Given
        testTag.setArticleCount(3);
        when(tagMapper.selectById(1L)).thenReturn(testTag);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            tagService.deleteTag(1L);
        });

        assertEquals("该标签下还有文章，无法删除", exception.getMessage());
        verify(tagMapper, never()).deleteById(any());
    }

    @Test
    @DisplayName("检查标签名称是否存在 - 存在")
    void existsByName_True() {
        // Given
        when(tagMapper.existsByName("Java")).thenReturn(true);

        // When
        boolean result = tagService.existsByName("Java");

        // Then
        assertTrue(result);
        verify(tagMapper).existsByName("Java");
    }

    @Test
    @DisplayName("检查标签名称是否存在 - 不存在")
    void existsByName_False() {
        // Given
        when(tagMapper.existsByName("NonExistent")).thenReturn(false);

        // When
        boolean result = tagService.existsByName("NonExistent");

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("搜索标签 - 成功")
    void searchTagsByName_Success() {
        // Given
        List<Tag> searchResults = Arrays.asList(testTag);
        when(tagMapper.selectTagsByNameLike("Java", 20)).thenReturn(searchResults);

        // When
        List<Tag> result = tagService.searchTagsByName("Java", 20);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(tagMapper).selectTagsByNameLike("Java", 20);
    }
}