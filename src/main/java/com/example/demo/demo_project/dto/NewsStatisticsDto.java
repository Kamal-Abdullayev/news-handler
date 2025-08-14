package com.example.demo.demo_project.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record NewsStatisticsDto(
        int totalNews,
        Map<String, Long> sourceStatistics,
        Map<String, Long> authorStatistics,
        LocalDateTime oldestNewsDate,
        LocalDateTime newestNewsDate,
        double averageContentLength,
        LocalDateTime generatedAt
) {}
