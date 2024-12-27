package com.example.demo.demo_project.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class News {
    private long id;
    private String author;
    private String title;
    private String description;
    private String url;
    private String urlToImage;
    private String localUrlToImage;
    private LocalDateTime publishedAt;
    private String content;
    private Source source;

}
