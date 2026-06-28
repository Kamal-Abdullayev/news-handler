package com.example.demo.demo_project.dto;

import jakarta.validation.constraints.NotBlank;

public record NewsDeleteRequestDto (
        @NotBlank
        String title
) {
}
