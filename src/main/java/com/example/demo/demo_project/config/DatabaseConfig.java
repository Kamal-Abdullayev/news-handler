package com.example.demo.demo_project.config;

import com.example.demo.demo_project.constants.DatabaseConfigConstant;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@RequiredArgsConstructor
@Configuration
public class DatabaseConfig {

    private final DatabaseConfigConstant databaseConfigConstant;

    @Bean
    public DataSource dataSource() {
        return DataSourceBuilder.create()
                .driverClassName(databaseConfigConstant.getDriverClassName())
                .url(databaseConfigConstant.getUrl())
                .username(databaseConfigConstant.getUsername())
                .password(databaseConfigConstant.getPassword())
                .build();
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

}
