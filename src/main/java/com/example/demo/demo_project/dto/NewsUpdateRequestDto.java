package com.example.demo.demo_project.dto;

import javax.validation.constraints.NotBlank;

public record NewsUpdateRequestDto(
        @NotBlank
        String author,
        @NotBlank
        String title,
        @NotBlank
        String description,
        @NotBlank
        String content,
        long sourceId
) {
}
