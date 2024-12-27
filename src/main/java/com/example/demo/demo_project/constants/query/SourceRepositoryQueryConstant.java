package com.example.demo.demo_project.constants.query;

public class SourceRepositoryQueryConstant {
    public static final String SAVE_SOURCE = "INSERT INTO source (id, name) VALUES (?, ?)";
    public static final String FIND_SOURCE = "SELECT * FROM source WHERE source_id = ?";
    public static final String FIND_ALL_SOURCE = "SELECT * FROM source ORDER BY source_id LIMIT ?";
    public static final String UPDATE_SOURCE = "UPDATE source SET id = ?, name = ? WHERE source_id = ?";
    public static final String DELETE_SOURCE = "DELETE FROM source WHERE source_id = ?";

}
