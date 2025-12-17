package com.example.blog.service;

import com.example.blog.dto.SensitiveWordRequest;
import com.example.blog.entity.SensitiveWord;
import com.example.blog.mapper.SensitiveWordMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SensitiveWordServiceTest {

    @Mock
    private SensitiveWordMapper sensitiveWordMapper;

    @InjectMocks
    private SensitiveWordServiceImpl sensitiveWordService;

    private List<SensitiveWord> mockWords;

    @BeforeEach
    void setUp() {
        // 创建测试敏感词
        SensitiveWord filterWord = new SensitiveWord();
        filterWord.setId(1L);
        filterWord.setWord("测试");
        filterWord.setReplacement("***");
        filterWord.setPattern("测试");
        filterWord.setType(SensitiveWord.WordType.FILTER.getValue());
        filterWord.setCreateTime(LocalDateTime.now());

        SensitiveWord blockWord = new SensitiveWord();
        blockWord.setId(2L);
        blockWord.setWord("违法");
        blockWord.setPattern("违法");
        blockWord.setType(SensitiveWord.WordType.BLOCK.getValue());
        blockWord.setCreateTime(LocalDateTime.now());

        SensitiveWord warningWord = new SensitiveWord();
        warningWord.setId(3L);
        warningWord.setWord("警告");
        warningWord.setPattern("警告");
        warningWord.setType(SensitiveWord.WordType.WARNING.getValue());
        warningWord.setCreateTime(LocalDateTime.now());

        mockWords = Arrays.asList(filterWord, blockWord, warningWord);
    }

    @Test
    void testFilterSensitiveWords_HasFilterWords() {
        // Given
        String text = "这是一个测试评论";
        when(sensitiveWordMapper.selectWordsByType(SensitiveWord.WordType.FILTER.getValue()))
                .thenReturn(Arrays.asList(mockWords.get(0)));

        // When
        String result = sensitiveWordService.filterSensitiveWords(text);

        // Then
        assertEquals("这是一个***评论", result);
    }

    @Test
    void testFilterSensitiveWords_NoFilterWords() {
        // Given
        String text = "这是正常评论";
        when(sensitiveWordMapper.selectWordsByType(SensitiveWord.WordType.FILTER.getValue()))
                .thenReturn(Collections.emptyList());

        // When
        String result = sensitiveWordService.filterSensitiveWords(text);

        // Then
        assertEquals(text, result);
    }

    @Test
    void testFilterSensitiveWords_EmptyText() {
        // When
        String result = sensitiveWordService.filterSensitiveWords("");

        // Then
        assertEquals("", result);

        // When
        result = sensitiveWordService.filterSensitiveWords(null);

        // Then
        assertNull(result);
    }

    @Test
    void testContainsBlockedWords_HasBlockedWords() {
        // Given
        String text = "这包含违法内容";
        when(sensitiveWordMapper.selectWordsByType(SensitiveWord.WordType.BLOCK.getValue()))
                .thenReturn(Arrays.asList(mockWords.get(1)));

        // When
        boolean result = sensitiveWordService.containsBlockedWords(text);

        // Then
        assertTrue(result);
    }

    @Test
    void testContainsBlockedWords_NoBlockedWords() {
        // Given
        String text = "这是正常评论";
        when(sensitiveWordMapper.selectWordsByType(SensitiveWord.WordType.BLOCK.getValue()))
                .thenReturn(Collections.emptyList());

        // When
        boolean result = sensitiveWordService.containsBlockedWords(text);

        // Then
        assertFalse(result);
    }

    @Test
    void testContainsBlockedWords_EmptyText() {
        // Given
        when(sensitiveWordMapper.selectWordsByType(SensitiveWord.WordType.BLOCK.getValue()))
                .thenReturn(Arrays.asList(mockWords.get(1)));

        // When
        boolean result = sensitiveWordService.containsBlockedWords("");

        // Then
        assertFalse(result);
    }

    @Test
    void testGetWarningWords_HasWarningWords() {
        // Given
        String text = "这包含警告内容";
        when(sensitiveWordMapper.selectWordsByType(SensitiveWord.WordType.WARNING.getValue()))
                .thenReturn(Arrays.asList(mockWords.get(2)));

        // When
        List<String> result = sensitiveWordService.getWarningWords(text);

        // Then
        assertEquals(1, result.size());
        assertEquals("警告", result.get(0));
    }

    @Test
    void testGetWarningWords_NoWarningWords() {
        // Given
        String text = "这是正常评论";
        when(sensitiveWordMapper.selectWordsByType(SensitiveWord.WordType.WARNING.getValue()))
                .thenReturn(Collections.emptyList());

        // When
        List<String> result = sensitiveWordService.getWarningWords(text);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void testAddSensitiveWord_Success() {
        // Given
        SensitiveWordRequest request = new SensitiveWordRequest();
        request.setWord("新词");
        request.setReplacement("***");
        request.setPattern("新词");
        request.setType(SensitiveWord.WordType.FILTER.getValue());

        when(sensitiveWordMapper.existsByWord("新词")).thenReturn(false);
        when(sensitiveWordMapper.insert(any(SensitiveWord.class))).thenReturn(1);

        // When
        SensitiveWord result = sensitiveWordService.addSensitiveWord(request);

        // Then
        assertNotNull(result);
        assertEquals("新词", result.getWord());
        assertEquals("***", result.getReplacement());
        assertEquals(SensitiveWord.WordType.FILTER.getValue(), result.getType());
        verify(sensitiveWordMapper).insert(any(SensitiveWord.class));
    }

    @Test
    void testAddSensitiveWord_WordExists() {
        // Given
        SensitiveWordRequest request = new SensitiveWordRequest();
        request.setWord("测试");
        request.setType(SensitiveWord.WordType.FILTER.getValue());

        when(sensitiveWordMapper.existsByWord("测试")).thenReturn(true);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            sensitiveWordService.addSensitiveWord(request);
        });
        verify(sensitiveWordMapper, never()).insert(any(SensitiveWord.class));
    }

    @Test
    void testUpdateSensitiveWord_Success() {
        // Given
        Long wordId = 1L;
        SensitiveWordRequest request = new SensitiveWordRequest();
        request.setWord("更新词");
        request.setReplacement("###");
        request.setPattern("更新词");
        request.setType(SensitiveWord.WordType.BLOCK.getValue());

        SensitiveWord existingWord = new SensitiveWord();
        existingWord.setId(wordId);
        existingWord.setWord("原词");

        when(sensitiveWordMapper.selectById(wordId)).thenReturn(existingWord);
        when(sensitiveWordMapper.updateById(any(SensitiveWord.class))).thenReturn(1);

        // When
        SensitiveWord result = sensitiveWordService.updateSensitiveWord(wordId, request);

        // Then
        assertNotNull(result);
        assertEquals("更新词", result.getWord());
        assertEquals("###", result.getReplacement());
        assertEquals(SensitiveWord.WordType.BLOCK.getValue(), result.getType());
        verify(sensitiveWordMapper).updateById(any(SensitiveWord.class));
    }

    @Test
    void testUpdateSensitiveWord_NotFound() {
        // Given
        Long wordId = 999L;
        SensitiveWordRequest request = new SensitiveWordRequest();
        request.setWord("更新词");
        request.setType(SensitiveWord.WordType.FILTER.getValue());

        when(sensitiveWordMapper.selectById(wordId)).thenReturn(null);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            sensitiveWordService.updateSensitiveWord(wordId, request);
        });
        verify(sensitiveWordMapper, never()).updateById(any(SensitiveWord.class));
    }

    @Test
    void testGetWordsByType() {
        // Given
        String type = SensitiveWord.WordType.FILTER.getValue();
        when(sensitiveWordMapper.selectWordsByType(type))
                .thenReturn(Arrays.asList(mockWords.get(0)));

        // When
        List<SensitiveWord> result = sensitiveWordService.getWordsByType(type);

        // Then
        assertEquals(1, result.size());
        assertEquals("测试", result.get(0).getWord());
        assertEquals(SensitiveWord.WordType.FILTER.getValue(), result.get(0).getType());
    }

    @Test
    void testWordExists_True() {
        // Given
        String word = "测试";
        when(sensitiveWordMapper.existsByWord(word)).thenReturn(true);

        // When
        boolean result = sensitiveWordService.wordExists(word);

        // Then
        assertTrue(result);
    }

    @Test
    void testWordExists_False() {
        // Given
        String word = "不存在";
        when(sensitiveWordMapper.existsByWord(word)).thenReturn(false);

        // When
        boolean result = sensitiveWordService.wordExists(word);

        // Then
        assertFalse(result);
    }

    @Test
    void testComplexPatternMatching() {
        // Given
        String text = "联系方式：123-456-7890";
        SensitiveWord phoneWord = new SensitiveWord();
        phoneWord.setId(4L);
        phoneWord.setWord("电话");
        phoneWord.setReplacement("[联系方式已屏蔽]");
        phoneWord.setPattern("\\d{3}-\\d{3}-\\d{4}");
        phoneWord.setType(SensitiveWord.WordType.FILTER.getValue());

        when(sensitiveWordMapper.selectWordsByType(SensitiveWord.WordType.FILTER.getValue()))
                .thenReturn(Arrays.asList(phoneWord));

        // When
        String result = sensitiveWordService.filterSensitiveWords(text);

        // Then
        assertEquals("联系方式：[联系方式已屏蔽]", result);
    }

    @Test
    void testMultipleFilterWords() {
        // Given
        String text = "测试和违法内容";
        SensitiveWord filterWord = new SensitiveWord();
        filterWord.setWord("测试");
        filterWord.setReplacement("***");
        filterWord.setPattern("测试");
        filterWord.setType(SensitiveWord.WordType.FILTER.getValue());

        when(sensitiveWordMapper.selectWordsByType(SensitiveWord.WordType.FILTER.getValue()))
                .thenReturn(Arrays.asList(filterWord));

        // When
        String result = sensitiveWordService.filterSensitiveWords(text);

        // Then
        assertEquals("***和违法内容", result);
    }

    @Test
    void testCaseInsensitiveMatching() {
        // Given
        String text = "TEST和test";
        SensitiveWord filterWord = new SensitiveWord();
        filterWord.setWord("test");
        filterWord.setReplacement("***");
        filterWord.setPattern("(?i)test");
        filterWord.setType(SensitiveWord.WordType.FILTER.getValue());

        when(sensitiveWordMapper.selectWordsByType(SensitiveWord.WordType.FILTER.getValue()))
                .thenReturn(Arrays.asList(filterWord));

        // When
        String result = sensitiveWordService.filterSensitiveWords(text);

        // Then
        assertEquals("***和***", result);
    }
}