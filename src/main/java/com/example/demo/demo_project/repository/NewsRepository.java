package com.example.demo.demo_project.repository;

import com.example.demo.demo_project.constants.query.NewsRepositoryQueryConstant;
import com.example.demo.demo_project.entity.News;
import com.example.demo.demo_project.entity.Source;
import com.example.demo.demo_project.exception.ResourceNotFoundException;
import com.example.demo.demo_project.service.SourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class NewsRepository {
    private final JdbcTemplate jdbcTemplate;
    private final SourceRepository sourceRepository;
    private final SourceService sourceService;

    private final RowMapper<News> newsRowMapper = new RowMapper<>() {
        @Override
        public News mapRow(ResultSet rs, int rowNum) throws SQLException {
            Optional<Source> source = sourceRepository.findSourceBySourceId(rs.getInt("source_id"));
            if (source.isEmpty())
                throw new ResourceNotFoundException("Source not found");

            return News.builder()
                    .id(rs.getLong("id"))
                    .author(rs.getString("author"))
                    .title(rs.getString("title"))
                    .description(rs.getString("description"))
                    .url(rs.getString("url"))
                    .urlToImage(rs.getString("url_to_image"))
                    .localUrlToImage(rs.getString("local_url_to_image"))
                    .publishedAt(rs.getTimestamp("published_at").toLocalDateTime())
                    .content(rs.getString("content"))
                    .source(source.get())
                    .build();
        }
    };

    public void saveNews(News news) {
        jdbcTemplate.update(NewsRepositoryQueryConstant.SAVE_NEWS,
                news.getAuthor(),
                news.getTitle(),
                news.getDescription(),
                news.getUrl(),
                news.getUrlToImage(),
                news.getLocalUrlToImage(),
                news.getPublishedAt(),
                news.getContent(),
                findNewsSourceId(news)
        );
    }

    @Transactional
    public List<News> saveAll(List<News> newsList, int batchSize) {
        jdbcTemplate.batchUpdate(NewsRepositoryQueryConstant.SAVE_NEWS,
                newsList,
                batchSize,
                (PreparedStatement ps, News news) -> {
                    ps.setString(1, news.getAuthor());
                    ps.setString(2, news.getTitle());
                    ps.setString(3, news.getDescription());
                    ps.setString(4, news.getUrl());
                    ps.setString(5, news.getUrlToImage());
                    ps.setString(6, news.getLocalUrlToImage());
                    ps.setTimestamp(7, Timestamp.valueOf(news.getPublishedAt()));
                    ps.setString(8, news.getContent());
                    ps.setLong(9, findNewsSourceId(news));
                });

        return newsList;
    }

    private long findNewsSourceId(News news) {
        Optional<Source> source = sourceRepository.findSourceByIdAndName(news.getSource().getId(), news.getSource().getName());
        if (source.isPresent()) {
            return source.get().getSourceId();
        }
        sourceRepository.saveSource(news.getSource());
        Source savedSource = sourceService.getSourceByIdAndName(news.getSource().getId(), news.getSource().getName());
        return savedSource.getSourceId();
    }

    public Optional<News> findByTitle(String title) {
        List<News> results = jdbcTemplate.query(NewsRepositoryQueryConstant.FIND_NEWS_BY_TITLE,
                new Object[]{title}, newsRowMapper);

        return results.stream().findFirst();
    }


    public Optional<News> findNewsById(long id) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    NewsRepositoryQueryConstant.FIND_NEWS_BY_ID, newsRowMapper, id));
        } catch (EmptyResultDataAccessException e) {
            throw new ResourceNotFoundException("News not found with id " + id);
        }
    }

    public List<News> findAllNews(int limit) {
        return jdbcTemplate.query(NewsRepositoryQueryConstant.FIND_ALL_NEWS,
                newsRowMapper, limit);
    }

    public Optional<News> updateNews(long id, String author, String title, String description, String content, long sourceId) {
        int rowsAffected = jdbcTemplate.update(NewsRepositoryQueryConstant.UPDATE_NEWS,
                author, title, description, content, sourceId, id);
        return rowsAffected > 0 ? findNewsById(id) : Optional.empty();
    }

    public boolean deleteNewsById(long id) {
        int rowsAffected = jdbcTemplate.update(NewsRepositoryQueryConstant.DELETE_NEWS_BY_ID, id);
        return rowsAffected > 0;
    }

    public boolean deleteNewsByTitle(String title) {
        int rowsAffected = jdbcTemplate.update(NewsRepositoryQueryConstant.DELETE_NEWS_BY_TITLE, title);
        return rowsAffected > 0;
    }


}
