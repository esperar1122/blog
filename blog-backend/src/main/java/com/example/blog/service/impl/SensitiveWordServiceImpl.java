package com.example.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.blog.dto.SensitiveWordRequest;
import com.example.blog.entity.SensitiveWord;
import com.example.blog.exception.BusinessException;
import com.example.blog.mapper.SensitiveWordMapper;
import com.example.blog.service.SensitiveWordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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

    private List<SensitiveWord> activeWordsCache;
    private List<String> wordCache;
    private List<Pattern> regexCache;
    private long lastCacheTime = 0;
    private static final long CACHE_TTL = 5 * 60 * 1000;

    // Epic5 CRUD operations
    @Override
    public IPage<SensitiveWord> getWordList(int page, int size, String keyword, String status) {
        Page<SensitiveWord> pageObj = new Page<>(page, size);
        QueryWrapper<SensitiveWord> queryWrapper = new QueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            queryWrapper.like("word", keyword);
        }

        if (StringUtils.hasText(status)) {
            queryWrapper.eq("status", status);
        }

        queryWrapper.orderByDesc("create_time");

        return sensitiveWordMapper.selectPage(pageObj, queryWrapper);
    }

    @Override
    @Transactional
    public SensitiveWord addWord(SensitiveWord sensitiveWord) {
        if (wordExists(sensitiveWord.getWord())) {
            throw new BusinessException("敏感词已存在");
        }

        sensitiveWordMapper.insert(sensitiveWord);
        refreshCache();

        log.info("添加敏感词: {}", sensitiveWord.getWord());
        return sensitiveWord;
    }

    @Override
    @Transactional
    public SensitiveWord updateWord(Long id, SensitiveWord sensitiveWord) {
        SensitiveWord existing = sensitiveWordMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("敏感词不存在");
        }

        if (!existing.getWord().equals(sensitiveWord.getWord())) {
            if (wordExists(sensitiveWord.getWord())) {
                throw new BusinessException("敏感词已存在");
            }
        }

        sensitiveWord.setId(id);
        sensitiveWordMapper.updateById(sensitiveWord);
        refreshCache();

        return sensitiveWord;
    }

    @Override
    @Transactional
    public void deleteWord(Long id) {
        SensitiveWord existing = sensitiveWordMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("敏感词不存在");
        }

        sensitiveWordMapper.deleteById(id);
        refreshCache();

        log.info("删除敏感词: {}", existing.getWord());
    }

    @Override
    @Transactional
    public void updateWordStatus(Long id, String status) {
        SensitiveWord existing = sensitiveWordMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("敏感词不存在");
        }

        existing.setStatus(status);
        sensitiveWordMapper.updateById(existing);
        refreshCache();
    }

    @Override
    public List<SensitiveWord> getActiveWords() {
        refreshCacheIfNeeded();
        return activeWordsCache;
    }

    // Common filtering methods
    @Override
    public String filterSensitiveWords(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        refreshCacheIfNeeded();
        String result = text;

        // Apply regex patterns first
        for (Pattern pattern : regexCache) {
            try {
                Matcher matcher = pattern.matcher(result);
                result = matcher.replaceAll("***");
            } catch (Exception e) {
                log.error("Error processing regex pattern", e);
            }
        }

        // Apply word filters
        for (String word : wordCache) {
            try {
                result = result.replaceAll("(?i)" + Pattern.quote(word), "***");
            } catch (Exception e) {
                log.error("Error processing word filter: {}", word, e);
            }
        }

        return result;
    }

    @Override
    public boolean containsSensitiveWord(String content) {
        if (content == null || content.isEmpty()) {
            return false;
        }

        refreshCacheIfNeeded();

        // Check regex patterns
        for (Pattern pattern : regexCache) {
            try {
                if (pattern.matcher(content).find()) {
                    return true;
                }
            } catch (Exception e) {
                log.error("Error checking regex pattern", e);
            }
        }

        // Check word filters
        for (String word : wordCache) {
            try {
                if (content.toLowerCase().contains(word.toLowerCase())) {
                    return true;
                }
            } catch (Exception e) {
                log.error("Error checking word filter: {}", word, e);
            }
        }

        return false;
    }

    @Override
    public boolean containsBlockedWords(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }

        refreshCacheIfNeeded();

        // Check if any word would be blocked (simplified check)
        return containsSensitiveWord(text);
    }

    // HEAD version specific methods
    @Override
    public List<String> getWarningWords(String text) {
        List<String> warningWords = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return warningWords;
        }

        refreshCacheIfNeeded();

        // Find words that match
        for (String word : wordCache) {
            try {
                if (text.toLowerCase().contains(word.toLowerCase())) {
                    warningWords.add(word);
                }
            } catch (Exception e) {
                log.error("Error checking warning word: {}", word, e);
            }
        }

        return warningWords;
    }

    @Override
    @Transactional
    public SensitiveWord addSensitiveWord(SensitiveWordRequest request) {
        SensitiveWord sensitiveWord = new SensitiveWord();
        sensitiveWord.setWord(request.getWord());
        sensitiveWord.setReplacement(request.getReplacement());
        sensitiveWord.setPattern(request.getPattern());
        sensitiveWord.setType(request.getType());

        return addWord(sensitiveWord);
    }

    @Override
    @Transactional
    public SensitiveWord updateSensitiveWord(Long id, SensitiveWordRequest request) {
        SensitiveWord existing = sensitiveWordMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("敏感词不存在");
        }

        existing.setWord(request.getWord());
        existing.setReplacement(request.getReplacement());
        existing.setPattern(request.getPattern());
        existing.setType(request.getType());

        return updateWord(id, existing);
    }

    @Override
    public List<SensitiveWord> getWordsByType(String type) {
        return baseMapper.selectWordsByType(type);
    }

    @Override
    public boolean wordExists(String word) {
        return baseMapper.existsByWord(word);
    }

    // Cache management
    private void refreshCache() {
        lastCacheTime = System.currentTimeMillis();
        activeWordsCache = sensitiveWordMapper.selectActiveWords();

        wordCache = new ArrayList<>();
        regexCache = new ArrayList<>();

        if (activeWordsCache != null) {
            for (SensitiveWord word : activeWordsCache) {
                if (word.isRegex() && word.getPattern() != null) {
                    try {
                        regexCache.add(Pattern.compile(word.getPattern(), Pattern.CASE_INSENSITIVE));
                    } catch (Exception e) {
                        log.error("Invalid regex pattern: {}", word.getPattern(), e);
                    }
                } else if (word.getWord() != null) {
                    wordCache.add(word.getWord());
                }
            }
        }
    }

    private void refreshCacheIfNeeded() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastCacheTime > CACHE_TTL || activeWordsCache == null) {
            refreshCache();
        }
    }
}