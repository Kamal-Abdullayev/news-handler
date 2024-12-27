package com.example.demo.demo_project.constants;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties(prefix = "project.scheduler")
@Component
public class SchedulerConfigConstant {
    private int pageSize;
    private String keyword;
}
