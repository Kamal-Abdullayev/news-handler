package com.example.demo.demo_project.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Source {
    private long sourceId;
    private String id;
    private String name;


    public Source(String id, String name) {
        this.id = id;
        this.name = name;
    }
}
