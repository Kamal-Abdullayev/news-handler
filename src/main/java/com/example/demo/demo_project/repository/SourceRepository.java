package com.example.demo.demo_project.repository;

import com.example.demo.demo_project.constants.query.SourceRepositoryQueryConstant;
import com.example.demo.demo_project.entity.Source;
import com.example.demo.demo_project.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class SourceRepository {
    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Source> sourceRowMapper = (ResultSet rs, int rowNum) -> {
        Source source = new Source();
        source.setSourceId(rs.getLong("source_id"));
        source.setId(rs.getString("id"));
        source.setName(rs.getString("name"));
        return source;
    };

    public void saveSource(Source source) {
        jdbcTemplate.update(SourceRepositoryQueryConstant.SAVE_SOURCE,
                source.getId(), source.getName());
    }

    public void saveAllSource(List<Source> sourceList, int batchSize) {
        jdbcTemplate.batchUpdate(SourceRepositoryQueryConstant.SAVE_SOURCE, sourceList, batchSize,
                (PreparedStatement ps, Source source) -> {
                    ps.setString(1, source.getId());
                    ps.setString(2, source.getName());
                });
    }

    public Optional<Source> findSourceBySourceId(long sourceId) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(SourceRepositoryQueryConstant.FIND_SOURCE,
                    sourceRowMapper, sourceId));
        } catch (EmptyResultDataAccessException e) {
            throw new ResourceNotFoundException("Source not found with id " + sourceId);
        }
    }

    public Optional<Source> findSourceByIdAndName(String id, String name) {
        StringBuilder findSourceQuery = new StringBuilder("SELECT * FROM source WHERE ");
        List<Object> parameters = new ArrayList<>();
        if (id != null) {
            findSourceQuery.append("id = ? ");
            parameters.add(id);
        } else {
            findSourceQuery.append("id IS NULL ");
        }
        findSourceQuery.append("AND ");
        if (name != null) {
            findSourceQuery.append("name = ?");
            parameters.add(name);
        } else {
            findSourceQuery.append("name IS NULL");
        }
        List<Source> results = jdbcTemplate.query(findSourceQuery.toString(), sourceRowMapper, parameters.toArray());
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public List<Source> findAllSources(int limit) {
        return jdbcTemplate.query(SourceRepositoryQueryConstant.FIND_ALL_SOURCE,
                sourceRowMapper, limit);
    }

    public Optional<Source> updateSource(Source source) {
        int rowsUpdated = jdbcTemplate.update(SourceRepositoryQueryConstant.UPDATE_SOURCE,
                source.getId(), source.getName(), source.getSourceId());
        return rowsUpdated > 0 ? findSourceBySourceId(source.getSourceId()) : Optional.empty();
    }

    public boolean deleteSourceById(long sourceId) {
        int rowsAffected = jdbcTemplate.update(SourceRepositoryQueryConstant.DELETE_SOURCE, sourceId);
        return rowsAffected > 0;
    }
}
