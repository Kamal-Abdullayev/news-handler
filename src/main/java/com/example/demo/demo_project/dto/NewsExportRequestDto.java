package com.example.demo.demo_project.dto;

import java.time.LocalDateTime;

public record NewsExportRequestDto(
        LocalDateTime startDate,
        LocalDateTime endDate,
        String source,
        String author,
        int limit
) {
    public NewsExportRequestDto {
        if (limit <= 0) limit = 100;
    }
}
