package com.example.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_operation_log")
@Schema(description = "操作日志实体")
public class OperationLog {

    @TableId(type = IdType.AUTO)
    @Schema(description = "日志ID")
    private Long id;

    @Schema(description = "操作用户ID")
    private Long userId;

    @Schema(description = "操作用户名")
    private String username;

    @Schema(description = "操作类型")
    private String operation;

    @Schema(description = "请求方法")
    private String method;

    @Schema(description = "请求参数")
    private String params;

    @Schema(description = "执行时长(毫秒)")
    private Long time;

    @Schema(description = "操作IP")
    private String ip;

    @Schema(description = "用户代理")
    private String userAgent;

    @Schema(description = "操作结果")
    private String result;

    @Schema(description = "操作状态 0:失败 1:成功")
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}