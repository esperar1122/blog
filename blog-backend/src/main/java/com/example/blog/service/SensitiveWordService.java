package com.example.blog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.blog.dto.SensitiveWordRequest;
import com.example.blog.entity.SensitiveWord;

import java.util.List;

public interface SensitiveWordService extends IService<SensitiveWord> {

    // Epic5 - Basic CRUD operations
    IPage<SensitiveWord> getWordList(int page, int size, String keyword, String status);

    SensitiveWord addWord(SensitiveWord sensitiveWord);

    SensitiveWord updateWord(Long id, SensitiveWord sensitiveWord);

    void deleteWord(Long id);

    void updateWordStatus(Long id, String status);

    List<SensitiveWord> getActiveWords();

    // Common methods from both versions
    String filterSensitiveWords(String text);

    boolean containsSensitiveWord(String content);

    boolean containsBlockedWords(String text);

    // HEAD version specific methods
    /**
     * Get warning words in text
     */
    List<String> getWarningWords(String text);

    /**
     * Add new sensitive word
     */
    SensitiveWord addSensitiveWord(SensitiveWordRequest request);

    /**
     * Update sensitive word
     */
    SensitiveWord updateSensitiveWord(Long id, SensitiveWordRequest request);

    /**
     * Get all sensitive words by type
     */
    List<SensitiveWord> getWordsByType(String type);

    /**
     * Check if word exists
     */
    boolean wordExists(String word);
}