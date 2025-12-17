package com.example.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.blog.dto.SensitiveWordRequest;
import com.example.blog.entity.SensitiveWord;

import java.util.List;

public interface SensitiveWordService extends IService<SensitiveWord> {

    /**
     * Filter sensitive words in text
     */
    String filterSensitiveWords(String text);

    /**
     * Check if text contains blocked words
     */
    boolean containsBlockedWords(String text);

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