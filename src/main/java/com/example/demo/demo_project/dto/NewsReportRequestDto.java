package com.example.demo.demo_project.dto;

import java.time.LocalDateTime;

public record NewsReportRequestDto(
        LocalDateTime startDate,
        LocalDateTime endDate,
        String source,
        String author,
        int limit,
        String reportType
) {
    public NewsReportRequestDto {
        if (limit <= 0) limit = 100;
        if (reportType == null) reportType = "COMPREHENSIVE";
    }
}
