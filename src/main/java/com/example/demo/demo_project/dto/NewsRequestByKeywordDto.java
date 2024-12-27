package com.example.demo.demo_project.dto;

import javax.validation.constraints.NotBlank;

public record NewsRequestByKeywordDto(
        @NotBlank
        String title
) {
}
