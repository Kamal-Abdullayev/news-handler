package com.example.demo.demo_project.dto;

public record TrendingTopicDto(
        String topic,
        Long frequency,
        double trendScore
) {}
