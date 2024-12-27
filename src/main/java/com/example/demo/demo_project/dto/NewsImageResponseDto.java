package com.example.demo.demo_project.dto;

import org.springframework.core.io.ByteArrayResource;

public record NewsImageResponseDto(
        ByteArrayResource byteArrayResource,
        long contentLength
) {
}
