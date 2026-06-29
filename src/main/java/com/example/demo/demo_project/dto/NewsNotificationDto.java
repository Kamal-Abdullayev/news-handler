package com.example.demo.demo_project.dto;

import java.time.LocalDateTime;

public record NewsNotificationDto(
        String id,
        String type,
        String title,
        String message,
        Long newsId,
        LocalDateTime createdAt,
        boolean isRead
) {}
