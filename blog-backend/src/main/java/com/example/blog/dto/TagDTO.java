package com.example.blog.dto;

import com.example.blog.entity.Tag;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 标签数据传输对象
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class TagDTO extends Tag {

    /**
     * 从实体转换为DTO
     */
    public static TagDTO fromEntity(Tag tag) {
        TagDTO dto = new TagDTO();
        dto.setId(tag.getId());
        dto.setName(tag.getName());
        dto.setColor(tag.getColor());
        dto.setArticleCount(tag.getArticleCount());
        dto.setCreateTime(tag.getCreateTime());
        dto.setUpdateTime(tag.getUpdateTime());
        return dto;
    }
}