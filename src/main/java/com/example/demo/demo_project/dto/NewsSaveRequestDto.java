package com.example.demo.demo_project.dto;

import javax.validation.constraints.NotBlank;

public record NewsSaveRequestDto(
        String author,
        @NotBlank(message = "Title cannot be blank")
        String title,
        String description,
        @NotBlank(message = "Url cannot be blank")
        String url,
        String urlToImage,
        @NotBlank(message = "Content cannot be blank")
        String content,
        long sourceId
) {

}
