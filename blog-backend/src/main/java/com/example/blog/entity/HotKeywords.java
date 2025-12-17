package com.example.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_hot_keywords")
public class HotKeywords {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("keyword")
    private String keyword;

    @TableField("search_count")
    private Integer searchCount;

    @TableField("position")
    private Integer position;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}