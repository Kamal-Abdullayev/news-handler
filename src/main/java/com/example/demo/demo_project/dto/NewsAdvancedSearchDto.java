package com.example.demo.demo_project.dto;

import java.time.LocalDateTime;

public record NewsAdvancedSearchDto(
        String keyword,
        String author,
        String source,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String sentiment,
        String sortBy,
        String sortOrder,
        int limit
) {
    public NewsAdvancedSearchDto {
        if (limit <= 0) limit = 10;
        if (sortBy == null) sortBy = "date";
        if (sortOrder == null) sortOrder = "desc";
    }
}
