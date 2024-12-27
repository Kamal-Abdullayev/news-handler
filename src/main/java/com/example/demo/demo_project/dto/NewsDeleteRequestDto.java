package com.example.demo.demo_project.dto;

import javax.validation.constraints.NotBlank;

public record NewsDeleteRequestDto (
        @NotBlank
        String title
) {
}
