package com.example.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.blog.dto.SensitiveWordRequest;
import com.example.blog.entity.SensitiveWord;
import com.example.blog.mapper.SensitiveWordMapper;
import com.example.blog.service.SensitiveWordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class SensitiveWordServiceImpl extends ServiceImpl<SensitiveWordMapper, SensitiveWord>
        implements SensitiveWordService {

    private final SensitiveWordMapper sensitiveWordMapper;

    @Override
    public String filterSensitiveWords(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        List<SensitiveWord> filterWords = baseMapper.selectWordsByType(SensitiveWord.WordType.FILTER.getValue());
        String result = text;

        for (SensitiveWord word : filterWords) {
            try {
                Pattern pattern = Pattern.compile(word.getPattern(), Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(result);
                String replacement = word.getReplacement() != null ? word.getReplacement() : "***";
                result = matcher.replaceAll(replacement);
            } catch (Exception e) {
                log.error("Error processing sensitive word pattern: {}", word.getPattern(), e);
            }
        }

        return result;
    }

    @Override
    public boolean containsBlockedWords(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }

        List<SensitiveWord> blockedWords = baseMapper.selectWordsByType(SensitiveWord.WordType.BLOCK.getValue());

        for (SensitiveWord word : blockedWords) {
            try {
                Pattern pattern = Pattern.compile(word.getPattern(), Pattern.CASE_INSENSITIVE);
                if (pattern.matcher(text).find()) {
                    return true;
                }
            } catch (Exception e) {
                log.error("Error checking blocked word pattern: {}", word.getPattern(), e);
            }
        }

        return false;
    }

    @Override
    public List<String> getWarningWords(String text) {
        List<String> warningWords = new ArrayList<>();

        if (text == null || text.isEmpty()) {
            return warningWords;
        }

        List<SensitiveWord> warningTypeWords = baseMapper.selectWordsByType(SensitiveWord.WordType.WARNING.getValue());

        for (SensitiveWord word : warningTypeWords) {
            try {
                Pattern pattern = Pattern.compile(word.getPattern(), Pattern.CASE_INSENSITIVE);
                if (pattern.matcher(text).find()) {
                    warningWords.add(word.getWord());
                }
            } catch (Exception e) {
                log.error("Error checking warning word pattern: {}", word.getPattern(), e);
            }
        }

        return warningWords;
    }

    @Override
    @Transactional
    public SensitiveWord addSensitiveWord(SensitiveWordRequest request) {
        if (wordExists(request.getWord())) {
            throw new RuntimeException("敏感词已存在");
        }

        SensitiveWord sensitiveWord = new SensitiveWord();
        sensitiveWord.setWord(request.getWord());
        sensitiveWord.setReplacement(request.getReplacement());
        sensitiveWord.setPattern(request.getPattern());
        sensitiveWord.setType(request.getType());

        save(sensitiveWord);
        log.info("Added new sensitive word: {}", request.getWord());

        return sensitiveWord;
    }

    @Override
    @Transactional
    public SensitiveWord updateSensitiveWord(Long id, SensitiveWordRequest request) {
        SensitiveWord sensitiveWord = getById(id);
        if (sensitiveWord == null) {
            throw new RuntimeException("敏感词不存在");
        }

        sensitiveWord.setWord(request.getWord());
        sensitiveWord.setReplacement(request.getReplacement());
        sensitiveWord.setPattern(request.getPattern());
        sensitiveWord.setType(request.getType());

        updateById(sensitiveWord);
        log.info("Updated sensitive word: {}", request.getWord());

        return sensitiveWord;
    }

    @Override
    public List<SensitiveWord> getWordsByType(String type) {
        return baseMapper.selectWordsByType(type);
    }

    @Override
    public boolean wordExists(String word) {
        Boolean exists = baseMapper.existsByWord(word);
        return exists != null && exists;
    }
}