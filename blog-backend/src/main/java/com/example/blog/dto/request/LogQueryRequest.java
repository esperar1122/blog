package com.example.blog.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.*;
import java.time.LocalDateTime;

@Data
@Schema(description = "日志查询请求")
public class LogQueryRequest {

    @Min(value = 1, message = "当前页不能小于1")
    @Schema(description = "当前页", example = "1")
    private Integer current = 1;

    @Min(value = 1, message = "每页大小不能小于1")
    @Max(value = 100, message = "每页大小不能超过100")
    @Schema(description = "每页大小", example = "10")
    private Integer size = 10;

    @Size(max = 50, message = "用户名长度不能超过50个字符")
    @Schema(description = "用户名")
    private String username;

    @Size(max = 100, message = "操作类型长度不能超过100个字符")
    @Schema(description = "操作类型")
    private String operation;

    @Size(max = 100, message = "请求方法长度不能超过100个字符")
    @Schema(description = "请求方法")
    private String method;

    @Pattern(regexp = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$",
             message = "IP地址格式不正确")
    @Schema(description = "IP地址")
    private String ip;

    @Pattern(regexp = "^[01]$", message = "操作状态只能是0或1")
    @Schema(description = "操作状态 0:失败 1:成功")
    private Integer status;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Size(max = 100, message = "关键词长度不能超过100个字符")
    @Schema(description = "关键词搜索")
    private String keyword;
}