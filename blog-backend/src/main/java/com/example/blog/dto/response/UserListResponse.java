package com.example.blog.dto.response;

import com.example.blog.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "用户列表响应")
public class UserListResponse {

    @Schema(description = "用户列表")
    private List<UserItem> users;

    @Schema(description = "总记录数")
    private Long total;

    @Schema(description = "当前页码")
    private Integer page;

    @Schema(description = "每页数量")
    private Integer size;

    @Schema(description = "总页数")
    private Integer totalPages;

    @Data
    @Schema(description = "用户信息")
    public static class UserItem {
        @Schema(description = "用户ID")
        private Long id;

        @Schema(description = "用户名")
        private String username;

        @Schema(description = "邮箱")
        private String email;

        @Schema(description = "昵称")
        private String nickname;

        @Schema(description = "头像")
        private String avatar;

        @Schema(description = "个人简介")
        private String bio;

        @Schema(description = "用户角色")
        private String role;

        @Schema(description = "用户状态")
        private String status;

        @Schema(description = "创建时间")
        private LocalDateTime createTime;

        @Schema(description = "更新时间")
        private LocalDateTime updateTime;

        @Schema(description = "最后登录时间")
        private LocalDateTime lastLoginTime;
    }
}