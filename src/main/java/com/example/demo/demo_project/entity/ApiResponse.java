package com.example.demo.demo_project.entity;

import lombok.Data;

import java.util.List;

@Data
public class ApiResponse {
    private String status;
    private int totalResults;
    private List<News> articles;

}
