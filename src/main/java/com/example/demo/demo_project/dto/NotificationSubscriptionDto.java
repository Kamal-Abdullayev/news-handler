package com.example.demo.demo_project.dto;

import java.util.List;

public record NotificationSubscriptionDto(
        String userId,
        String email,
        List<String> preferences
) {}
