package com.example.demo.demo_project.constants;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties(prefix = "spring.datasource")
@Component
public class DatabaseConfigConstant {
    private String driverClassName;
    private String url;
    private String username;
    private String password;

}
