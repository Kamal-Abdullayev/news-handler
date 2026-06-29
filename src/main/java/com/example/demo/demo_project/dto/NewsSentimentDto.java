package com.example.demo.demo_project.dto;

import java.time.LocalDateTime;
import java.util.Set;

public record NewsSentimentDto(
        Long newsId,
        String sentiment,
        double sentimentScore,
        Set<String> keywords,
        LocalDateTime analyzedAt
) {}
