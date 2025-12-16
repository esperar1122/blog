package com.example.blog.dto.request;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "用户查询请求")
public class UserQueryRequest {

    @Schema(description = "页码", example = "0")
    private Integer page = 0;

    @Schema(description = "每页数量", example = "20")
    private Integer size = 20;

    @Schema(description = "角色筛选", example = "USER")
    private String role;

    @Schema(description = "状态筛选", example = "ACTIVE")
    private String status;

    @Schema(description = "搜索关键词", example = "john")
    private String keyword;
}