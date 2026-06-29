package com.example.demo.demo_project.dto;

import java.time.LocalDateTime;

public record NewsExportDto(
        String filename,
        String contentType,
        byte[] content,
        int recordCount,
        LocalDateTime exportedAt
) {}
