package com.example.demo.demo_project.constants;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties(prefix = "spring.minio")
@Component
public class MinioConfigConstant {
    private Bucket bucket;
    private String url;
    private String accessKey;
    private String secretKey;


    @Data
    public static class Bucket {
        private String name;
    }
}
