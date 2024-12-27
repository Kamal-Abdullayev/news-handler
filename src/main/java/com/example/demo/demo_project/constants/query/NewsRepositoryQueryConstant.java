package com.example.demo.demo_project.constants.query;

public class NewsRepositoryQueryConstant {
     public static final String SAVE_NEWS = "INSERT INTO news (author, title, description, url, " +
            "url_to_image, local_url_to_image, published_at, content, source_id) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

     public static final String FIND_NEWS_BY_TITLE = "SELECT * FROM news WHERE title = ?";
     public static final String FIND_NEWS_BY_ID = "SELECT * FROM news WHERE id = ?";
     public static final String FIND_ALL_NEWS = "SELECT * FROM news ORDER BY id LIMIT ?";
     public static final String UPDATE_NEWS = "UPDATE news SET author = ?, title = ?, description = ?, " +
             "content = ?, source_id = ? WHERE id = ?";

     public static final String DELETE_NEWS_BY_ID = "DELETE FROM news WHERE id = ?";
     public static final String DELETE_NEWS_BY_TITLE = "DELETE FROM news WHERE title = ?";
}
