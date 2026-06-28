package com.example.demo.demo_project.dto;


import jakarta.validation.constraints.NotBlank;

public record SourceRequestDto(
        @NotBlank
        String id,
        @NotBlank
        String name
) {

}


