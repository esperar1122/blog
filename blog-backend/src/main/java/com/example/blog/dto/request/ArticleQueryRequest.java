package com.example.blog.dto.request;

import lombok.Data;
import javax.validation.constraints.Min;
import javax.validation.constraints.Max;
import javax.validation.constraints.Pattern;

@Data
public class ArticleQueryRequest {

    @Min(value = 0, message = "页码不能小于0")
    private Integer page = 0;

    @Min(value = 1, message = "每页数量不能小于1")
    @Max(value = 100, message = "每页数量不能超过100")
    private Integer size = 10;

    @Pattern(regexp = "^(publishTime|viewCount|likeCount|createTime)$", message = "排序字段无效")
    private String sortBy = "publishTime";

    @Pattern(regexp = "^(asc|desc)$", message = "排序方向无效")
    private String sortDir = "desc";

    private Long categoryId;

    private Long tagId;

    private String keyword;

    @Pattern(regexp = "^(PUBLISHED|DRAFT|ALL)$", message = "状态值无效")
    private String status = "PUBLISHED";

    private Long authorId;

    private Boolean isTop;
}