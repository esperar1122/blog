package com.example.blog.dto.response;

import lombok.Data;
import lombok.Builder;
import java.util.Map;

@Data
@Builder
public class ContentStatsResponse {

    private Long totalArticles;

    private Long publishedArticles;

    private Long draftArticles;

    private Long totalComments;

    private Long activeComments;

    private Long deletedComments;

    private Long todayArticles;

    private Long todayComments;

    private Map<String, Long> categoryStats;

    private Map<String, Long> monthlyStats;
}