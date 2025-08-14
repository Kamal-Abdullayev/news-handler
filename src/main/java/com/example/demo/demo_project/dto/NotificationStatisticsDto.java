package com.example.demo.demo_project.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record NotificationStatisticsDto(
        int totalSubscriptions,
        int totalNotifications,
        Map<String, Long> notificationTypeStats,
        LocalDateTime generatedAt
) {}
