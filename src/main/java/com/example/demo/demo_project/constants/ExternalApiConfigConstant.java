package com.example.demo.demo_project.constants;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties(prefix = "project.external-api")
@Component
public class ExternalApiConfigConstant {
    private String header;
    private String secretKey;
    private String url;

}
