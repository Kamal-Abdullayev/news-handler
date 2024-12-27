package com.example.demo.demo_project.constants;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties(prefix = "project.helper")
@Component
public class ProjectHelperConstant {
    private String baseUrl;
    private String cacheName;
    private int newsQueryLimit;
    private int newsBatchSize;
    private int sourceQueryLimit;
    private int sourceBatchSize;
    private String imageFolderPath;
    private String defaultDummyNewsSearchQuery;
}
