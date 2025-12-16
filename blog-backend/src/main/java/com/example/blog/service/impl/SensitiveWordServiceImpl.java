package com.example.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blog.entity.SensitiveWord;
import com.example.blog.exception.BusinessException;
import com.example.blog.mapper.SensitiveWordMapper;
import com.example.blog.service.SensitiveWordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

@Slf4j
@Service
@RequiredArgsConstructor
public class SensitiveWordServiceImpl implements SensitiveWordService {

    private final SensitiveWordMapper sensitiveWordMapper;

    private List<SensitiveWord> activeWordsCache;
    private List<String> wordCache;
    private List<Pattern> regexCache;
    private long lastCacheTime = 0;
    private static final long CACHE_TTL = 5 * 60 * 1000;

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
        QueryWrapper<SensitiveWord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("word", sensitiveWord.getWord());
        SensitiveWord existing = sensitiveWordMapper.selectOne(queryWrapper);

        if (existing != null) {
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
            QueryWrapper<SensitiveWord> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("word", sensitiveWord.getWord())
                       .ne("id", id);
            SensitiveWord duplicate = sensitiveWordMapper.selectOne(queryWrapper);
            if (duplicate != null) {
                throw new BusinessException("敏感词已存在");
            }
        }

        sensitiveWord.setId(id);
        sensitiveWordMapper.updateById(sensitiveWord);
        refreshCache();

        log.info("更新敏感词: {} -> {}", existing.getWord(), sensitiveWord.getWord());
        return sensitiveWord;
    }

    @Override
    @Transactional
    public void deleteWord(Long id) {
        SensitiveWord sensitiveWord = sensitiveWordMapper.selectById(id);
        if (sensitiveWord == null) {
            throw new BusinessException("敏感词不存在");
        }

        sensitiveWordMapper.deleteById(id);
        refreshCache();

        log.info("删除敏感词: {}", sensitiveWord.getWord());
    }

    @Override
    @Transactional
    public void updateWordStatus(Long id, String status) {
        SensitiveWord sensitiveWord = sensitiveWordMapper.selectById(id);
        if (sensitiveWord == null) {
            throw new BusinessException("敏感词不存在");
        }

        sensitiveWord.setStatus(status);
        sensitiveWordMapper.updateById(sensitiveWord);
        refreshCache();

        log.info("更新敏感词状态: {} -> {}", sensitiveWord.getWord(), status);
    }

    @Override
    public List<SensitiveWord> getActiveWords() {
        long currentTime = System.currentTimeMillis();
        if (activeWordsCache == null || (currentTime - lastCacheTime) > CACHE_TTL) {
            refreshCache();
        }
        return activeWordsCache;
    }

    @Override
    public boolean containsSensitiveWord(String content) {
        if (!StringUtils.hasText(content)) {
            return false;
        }

        List<SensitiveWord> words = getActiveWords();
        String lowerContent = content.toLowerCase();

        for (SensitiveWord word : words) {
            if (SensitiveWord.Type.REGEX.getValue().equals(word.getType())) {
                try {
                    Pattern pattern = Pattern.compile(word.getWord(), Pattern.CASE_INSENSITIVE);
                    if (pattern.matcher(content).find()) {
                        return true;
                    }
                } catch (Exception e) {
                    log.warn("正则表达式编译失败: {}", word.getWord(), e);
                }
            } else {
                if (lowerContent.contains(word.getWord().toLowerCase())) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public String filterSensitiveWords(String content) {
        if (!StringUtils.hasText(content)) {
            return content;
        }

        List<SensitiveWord> words = getActiveWords();
        String result = content;

        for (SensitiveWord word : words) {
            if (SensitiveWord.Type.REGEX.getValue().equals(word.getType())) {
                try {
                    Pattern pattern = Pattern.compile(word.getWord(), Pattern.CASE_INSENSITIVE);
                    Matcher matcher = pattern.matcher(result);
                    result = matcher.replaceAll("***");
                } catch (Exception e) {
                    log.warn("正则表达式过滤失败: {}", word.getWord(), e);
                }
            } else {
                String lowerWord = word.getWord().toLowerCase();
                String lowerResult = result.toLowerCase();
                int index = lowerResult.indexOf(lowerWord);
                while (index != -1) {
                    String stars = "*".repeat(word.getWord().length());
                    result = result.substring(0, index) + stars + result.substring(index + word.getWord().length());
                    lowerResult = result.toLowerCase();
                    index = lowerResult.indexOf(lowerWord, index + word.getWord().length());
                }
            }
        }

        return result;
    }

    private void refreshCache() {
        try {
            activeWordsCache = sensitiveWordMapper.selectActiveWords();

            wordCache = activeWordsCache.stream()
                .filter(word -> SensitiveWord.Type.WORD.getValue().equals(word.getType()))
                .map(SensitiveWord::getWord)
                .toList();

            regexCache = activeWordsCache.stream()
                .filter(word -> SensitiveWord.Type.REGEX.getValue().equals(word.getType()))
                .map(word -> {
                    try {
                        return Pattern.compile(word.getWord(), Pattern.CASE_INSENSITIVE);
                    } catch (Exception e) {
                        log.warn("编译正则表达式失败: {}", word.getWord(), e);
                        return null;
                    }
                })
                .filter(pattern -> pattern != null)
                .toList();

            lastCacheTime = System.currentTimeMillis();

            log.debug("刷新敏感词缓存，当前活跃敏感词数量: {}", activeWordsCache.size());
        } catch (Exception e) {
            log.error("刷新敏感词缓存失败", e);
        }
    }
}