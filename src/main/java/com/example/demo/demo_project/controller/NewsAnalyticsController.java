package com.example.demo.demo_project.controller;

import com.example.demo.demo_project.dto.*;
import com.example.demo.demo_project.service.NewsAnalyticsService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/v1/analytics")
@RestController
@RateLimiter(name = "demo-project")
@Slf4j
public class NewsAnalyticsController {
    private final NewsAnalyticsService newsAnalyticsService;

    @GetMapping("/sentiment/{newsId}")
    public ResponseEntity<NewsSentimentDto> analyzeNewsSentiment(@PathVariable Long newsId) {
        log.info("Analyzing sentiment for news with id: {}", newsId);
        return new ResponseEntity<>(newsAnalyticsService.analyzeNewsSentiment(newsId), HttpStatus.OK);
    }

    @GetMapping("/trending")
    public ResponseEntity<List<TrendingTopicDto>> getTrendingTopics(
            @RequestParam(defaultValue = "7") int days) {
        log.info("Getting trending topics for the last {} days", days);
        return new ResponseEntity<>(newsAnalyticsService.getTrendingTopics(days), HttpStatus.OK);
    }

    @GetMapping("/recommendations/{newsId}")
    public ResponseEntity<List<NewsRecommendationDto>> getNewsRecommendations(
            @PathVariable Long newsId,
            @RequestParam(defaultValue = "5") int limit) {
        log.info("Getting news recommendations for news with id: {}", newsId);
        return new ResponseEntity<>(newsAnalyticsService.getNewsRecommendations(newsId, limit), HttpStatus.OK);
    }

    @GetMapping("/statistics")
    public ResponseEntity<NewsStatisticsDto> getNewsStatistics() {
        log.info("Getting news statistics");
        return new ResponseEntity<>(newsAnalyticsService.getNewsStatistics(), HttpStatus.OK);
    }

    @PostMapping("/search")
    public ResponseEntity<List<NewsResponseDto>> advancedSearch(
            @Valid @RequestBody NewsAdvancedSearchDto searchDto) {
        log.info("Performing advanced search with criteria: {}", searchDto);
        return new ResponseEntity<>(newsAnalyticsService.advancedSearch(searchDto), HttpStatus.OK);
    }
}
