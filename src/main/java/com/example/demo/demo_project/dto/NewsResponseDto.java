package com.example.demo.demo_project.dto;

import com.example.demo.demo_project.entity.News;

import java.time.LocalDateTime;

public record NewsResponseDto (
        String author,
        String title,
        String description,
        String url,
        String urlToImage,
        LocalDateTime publishedAt,
        String content,
        SourceDto sourceDto
) {

    public static NewsResponseDto convert(News news) {

        String imageUrl = news.getLocalUrlToImage() != null ?
                "http://localhost:8090" + news.getLocalUrlToImage() : null;

        return new NewsResponseDto(
                news.getAuthor(),
                news.getTitle(),
                news.getDescription(),
                news.getUrl(),
                imageUrl,
                news.getPublishedAt(),
                news.getContent(),
                SourceDto.convert(news.getSource())
        );
    }
}
