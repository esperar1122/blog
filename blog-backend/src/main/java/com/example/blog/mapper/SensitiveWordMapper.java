package com.example.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blog.entity.SensitiveWord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface SensitiveWordMapper extends BaseMapper<SensitiveWord> {

    @Select("SELECT * FROM t_sensitive_word WHERE status = 'ACTIVE'")
    List<SensitiveWord> selectActiveWords();

    @Select("SELECT * FROM t_sensitive_word WHERE status = 'ACTIVE' AND type = 'WORD'")
    List<SensitiveWord> selectActiveWordsOnly();

    @Select("SELECT * FROM t_sensitive_word WHERE status = 'ACTIVE' AND type = 'REGEX'")
    List<SensitiveWord> selectActiveRegexOnly();
}