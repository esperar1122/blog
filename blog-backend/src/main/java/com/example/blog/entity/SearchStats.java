package com.example.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_search_stats")
public class SearchStats {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("keyword")
    private String keyword;

    @TableField("search_count")
    private Integer searchCount;

    @TableField("avg_result_count")
    private BigDecimal avgResultCount;

    @TableField(value = "last_search_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime lastSearchTime;
}