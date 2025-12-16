package com.example.blog.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Data
@Schema(description = "定时发布文章请求")
public class SchedulePublishRequest {

    @NotNull(message = "文章ID不能为空")
    @Schema(description = "文章ID", required = true)
    private Long articleId;

    @NotNull(message = "发布时间不能为空")
    @Future(message = "发布时间必须是未来时间")
    @Schema(description = "定时发布时间", required = true)
    private LocalDateTime scheduledPublishTime;

    @Schema(description = "定时发布备注")
    private String scheduleNote;
}