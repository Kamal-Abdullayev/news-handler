package com.example.demo.demo_project.dto;

import com.example.demo.demo_project.entity.News;

public record NewsRecommendationDto(
        News news,
        double similarity
) {}
