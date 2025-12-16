package com.example.blog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.blog.entity.SensitiveWord;

import java.util.List;

public interface SensitiveWordService {

    IPage<SensitiveWord> getWordList(int page, int size, String keyword, String status);

    SensitiveWord addWord(SensitiveWord sensitiveWord);

    SensitiveWord updateWord(Long id, SensitiveWord sensitiveWord);

    void deleteWord(Long id);

    void updateWordStatus(Long id, String status);

    List<SensitiveWord> getActiveWords();

    boolean containsSensitiveWord(String content);

    String filterSensitiveWords(String content);
}