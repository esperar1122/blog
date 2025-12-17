package com.example.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blog.entity.SensitiveWord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface SensitiveWordMapper extends BaseMapper<SensitiveWord> {

    /**
     * Get sensitive words by type
     */
    List<SensitiveWord> selectWordsByType(@Param("type") String type);

    /**
     * Get all active sensitive words for filtering
     */
    List<SensitiveWord> selectAllActiveWords();

    /**
     * Check if word already exists
     */
    Boolean existsByWord(@Param("word") String word);

    /**
     * Get words that match a pattern
     */
    List<SensitiveWord> selectWordsByPattern(@Param("pattern") String pattern);

    /**
     * Count sensitive words by type
     */
    Long countWordsByType(@Param("type") String type);

    // Additional methods from epic5
    @Select("SELECT * FROM t_sensitive_word WHERE status = 'ACTIVE'")
    List<SensitiveWord> selectActiveWords();

    @Select("SELECT * FROM t_sensitive_word WHERE status = 'ACTIVE' AND type = 'WORD'")
    List<SensitiveWord> selectActiveWordsOnly();

    @Select("SELECT * FROM t_sensitive_word WHERE status = 'ACTIVE' AND type = 'REGEX'")
    List<SensitiveWord> selectActiveRegexOnly();
}