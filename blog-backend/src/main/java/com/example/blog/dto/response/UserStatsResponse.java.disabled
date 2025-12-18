package com.example.blog.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户统计响应")
public class UserStatsResponse {

    @Schema(description = "总用户数")
    private Long totalUsers;

    @Schema(description = "活跃用户数")
    private Long activeUsers;

    @Schema(description = "禁用用户数")
    private Long bannedUsers;

    @Schema(description = "未激活用户数")
    private Long inactiveUsers;

    @Schema(description = "管理员数量")
    private Long adminUsers;

    @Schema(description = "普通用户数量")
    private Long normalUsers;

    @Schema(description = "今日注册用户数")
    private Long todayRegistrations;

    @Schema(description = "本周注册用户数")
    private Long weekRegistrations;

    @Schema(description = "本月注册用户数")
    private Long monthRegistrations;
}