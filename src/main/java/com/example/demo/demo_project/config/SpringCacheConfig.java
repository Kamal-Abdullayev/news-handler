package com.example.demo.demo_project.config;

import com.example.demo.demo_project.constants.ProjectHelperConstant;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@EnableCaching
@Configuration
public class SpringCacheConfig {
    private final ProjectHelperConstant projectHelperConstant;

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(projectHelperConstant.getCacheName());
    }
}
