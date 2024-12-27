package com.example.demo.demo_project.dto;


import javax.validation.constraints.NotBlank;

public record SourceRequestDto(
        @NotBlank
        String id,
        @NotBlank
        String name
) {

}


