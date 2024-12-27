package com.example.demo.demo_project.dto;

import com.example.demo.demo_project.entity.Source;

public record SourceDto(
        String id,
        String name
) {
    public static SourceDto convert(Source source) {
        return new SourceDto(
                source.getId(),
                source.getName()
        );
    }
}
