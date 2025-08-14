package com.example.demo.demo_project.service;

import com.example.demo.demo_project.constants.ProjectHelperConstant;
import com.example.demo.demo_project.dto.*;
import com.example.demo.demo_project.entity.News;
import com.example.demo.demo_project.exception.ResourceNotFoundException;
import com.example.demo.demo_project.repository.NewsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class NewsAnalyticsService {
    private final NewsRepository newsRepository;
    private final ProjectHelperConstant projectHelperConstant;

    /**
     * Analyze sentiment of news content using simple keyword-based approach
     */
    public NewsSentimentDto analyzeNewsSentiment(Long newsId) {
        log.info("Analyzing sentiment for news with id: {}", newsId);
        
        News news = newsRepository.findNewsById(newsId)
                .orElseThrow(() -> new ResourceNotFoundException("News not found with id: " + newsId));
        
        String content = (news.getContent() != null ? news.getContent() : "") + 
                        (news.getDescription() != null ? " " + news.getDescription() : "") +
                        (news.getTitle() != null ? " " + news.getTitle() : "");
        
        double sentimentScore = calculateSentimentScore(content.toLowerCase());
        String sentiment = determineSentiment(sentimentScore);
        
        return new NewsSentimentDto(
                newsId,
                sentiment,
                sentimentScore,
                extractKeywords(content),
                LocalDateTime.now()
        );
    }

    /**
     * Get trending topics based on news frequency in the last N days
     */
    @Cacheable(cacheNames = "demo-cache", key = "'trending_topics'")
    public List<TrendingTopicDto> getTrendingTopics(int days) {
        log.info("Getting trending topics for the last {} days", days);
        
        LocalDateTime cutoffDate = LocalDateTime.now().minus(days, ChronoUnit.DAYS);
        List<News> recentNews = newsRepository.findNewsByDateRange(cutoffDate, LocalDateTime.now());
        
        Map<String, Long> topicFrequency = recentNews.stream()
                .flatMap(news -> extractKeywords(getNewsText(news)).stream())
                .collect(Collectors.groupingBy(
                        keyword -> keyword,
                        Collectors.counting()
                ));
        
        return topicFrequency.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .map(entry -> new TrendingTopicDto(
                        entry.getKey(),
                        entry.getValue(),
                        calculateTrendScore(entry.getValue(), recentNews.size())
                ))
                .collect(Collectors.toList());
    }

    /**
     * Get news recommendations based on a given news article
     */
    public List<NewsRecommendationDto> getNewsRecommendations(Long newsId, int limit) {
        log.info("Getting news recommendations for news with id: {}", newsId);
        
        News sourceNews = newsRepository.findNewsById(newsId)
                .orElseThrow(() -> new ResourceNotFoundException("News not found with id: " + newsId));
        
        Set<String> sourceKeywords = extractKeywords(getNewsText(sourceNews));
        
        List<News> allNews = newsRepository.findAllNews(projectHelperConstant.getNewsQueryLimit());
        
        return allNews.stream()
                .filter(news -> !Objects.equals(news.getId(), newsId))
                .map(news -> {
                    Set<String> newsKeywords = extractKeywords(getNewsText(news));
                    double similarity = calculateSimilarity(sourceKeywords, newsKeywords);
                    return new NewsRecommendationDto(news, similarity);
                })
                .filter(rec -> rec.similarity() > 0.1) // Only return relevant recommendations
                .sorted(Comparator.comparing(NewsRecommendationDto::similarity).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Get comprehensive news statistics
     */
    @Cacheable(cacheNames = "demo-cache", key = "'news_statistics'")
    public NewsStatisticsDto getNewsStatistics() {
        log.info("Generating news statistics");
        
        List<News> allNews = newsRepository.findAllNews(projectHelperConstant.getNewsQueryLimit());
        
        // Source statistics
        Map<String, Long> sourceStats = allNews.stream()
                .collect(Collectors.groupingBy(
                        news -> news.getSource() != null ? news.getSource().getName() : "Unknown",
                        Collectors.counting()
                ));
        
        // Author statistics
        Map<String, Long> authorStats = allNews.stream()
                .filter(news -> news.getAuthor() != null && !news.getAuthor().trim().isEmpty())
                .collect(Collectors.groupingBy(
                        News::getAuthor,
                        Collectors.counting()
                ));
        
        // Date range statistics
        LocalDateTime oldestDate = allNews.stream()
                .map(News::getPublishedAt)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now());
        
        LocalDateTime newestDate = allNews.stream()
                .map(News::getPublishedAt)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now());
        
        // Average content length
        double avgContentLength = allNews.stream()
                .mapToInt(news -> getNewsText(news).length())
                .average()
                .orElse(0.0);
        
        return new NewsStatisticsDto(
                allNews.size(),
                sourceStats,
                authorStats,
                oldestDate,
                newestDate,
                avgContentLength,
                LocalDateTime.now()
        );
    }

    /**
     * Advanced search with multiple filters
     */
    public List<NewsResponseDto> advancedSearch(NewsAdvancedSearchDto searchDto) {
        log.info("Performing advanced search with criteria: {}", searchDto);
        
        List<News> allNews = newsRepository.findAllNews(projectHelperConstant.getNewsQueryLimit());
        
        return allNews.stream()
                .filter(news -> matchesKeyword(news, searchDto.keyword()))
                .filter(news -> matchesAuthor(news, searchDto.author()))
                .filter(news -> matchesSource(news, searchDto.source()))
                .filter(news -> matchesDateRange(news, searchDto.startDate(), searchDto.endDate()))
                .filter(news -> matchesSentiment(news, searchDto.sentiment()))
                .sorted(getSortComparator(searchDto.sortBy(), searchDto.sortOrder()))
                .limit(searchDto.limit())
                .map(NewsResponseDto::convert)
                .collect(Collectors.toList());
    }

    // Helper methods
    private double calculateSentimentScore(String text) {
        Set<String> positiveWords = Set.of("good", "great", "excellent", "amazing", "wonderful", "positive", "success", "win", "profit", "growth");
        Set<String> negativeWords = Set.of("bad", "terrible", "awful", "negative", "loss", "fail", "crisis", "problem", "issue", "decline");
        
        String[] words = text.split("\\s+");
        int positiveCount = 0;
        int negativeCount = 0;
        
        for (String word : words) {
            if (positiveWords.contains(word)) positiveCount++;
            if (negativeWords.contains(word)) negativeCount++;
        }
        
        if (words.length == 0) return 0.0;
        
        return (double) (positiveCount - negativeCount) / words.length;
    }

    private String determineSentiment(double score) {
        if (score > 0.01) return "POSITIVE";
        if (score < -0.01) return "NEGATIVE";
        return "NEUTRAL";
    }

    private Set<String> extractKeywords(String text) {
        // Simple keyword extraction - in a real implementation, you might use NLP libraries
        Set<String> stopWords = Set.of("the", "a", "an", "and", "or", "but", "in", "on", "at", "to", "for", "of", "with", "by", "is", "are", "was", "were", "be", "been", "have", "has", "had", "do", "does", "did", "will", "would", "could", "should", "may", "might", "can", "this", "that", "these", "those");
        
        return Arrays.stream(text.toLowerCase().split("\\s+"))
                .filter(word -> word.length() > 3 && !stopWords.contains(word))
                .limit(10)
                .collect(Collectors.toSet());
    }

    private String getNewsText(News news) {
        return (news.getContent() != null ? news.getContent() : "") + 
               (news.getDescription() != null ? " " + news.getDescription() : "") +
               (news.getTitle() != null ? " " + news.getTitle() : "");
    }

    private double calculateTrendScore(Long frequency, int totalNews) {
        return (double) frequency / totalNews * 100;
    }

    private double calculateSimilarity(Set<String> keywords1, Set<String> keywords2) {
        if (keywords1.isEmpty() || keywords2.isEmpty()) return 0.0;
        
        Set<String> intersection = new HashSet<>(keywords1);
        intersection.retainAll(keywords2);
        
        Set<String> union = new HashSet<>(keywords1);
        union.addAll(keywords2);
        
        return (double) intersection.size() / union.size();
    }

    private boolean matchesKeyword(News news, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return true;
        String text = getNewsText(news).toLowerCase();
        return text.contains(keyword.toLowerCase());
    }

    private boolean matchesAuthor(News news, String author) {
        if (author == null || author.trim().isEmpty()) return true;
        return news.getAuthor() != null && news.getAuthor().toLowerCase().contains(author.toLowerCase());
    }

    private boolean matchesSource(News news, String source) {
        if (source == null || source.trim().isEmpty()) return true;
        return news.getSource() != null && news.getSource().getName().toLowerCase().contains(source.toLowerCase());
    }

    private boolean matchesDateRange(News news, LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null && endDate == null) return true;
        if (news.getPublishedAt() == null) return false;
        
        if (startDate != null && news.getPublishedAt().isBefore(startDate)) return false;
        if (endDate != null && news.getPublishedAt().isAfter(endDate)) return false;
        
        return true;
    }

    private boolean matchesSentiment(News news, String sentiment) {
        if (sentiment == null || sentiment.trim().isEmpty()) return true;
        NewsSentimentDto sentimentDto = analyzeNewsSentiment(news.getId());
        return sentimentDto.sentiment().equalsIgnoreCase(sentiment);
    }

    private Comparator<News> getSortComparator(String sortBy, String sortOrder) {
        Comparator<News> comparator = switch (sortBy != null ? sortBy.toLowerCase() : "date") {
            case "title" -> Comparator.comparing(News::getTitle, Comparator.nullsLast(String::compareTo));
            case "author" -> Comparator.comparing(News::getAuthor, Comparator.nullsLast(String::compareTo));
            case "source" -> Comparator.comparing(news -> news.getSource() != null ? news.getSource().getName() : "", Comparator.nullsLast(String::compareTo));
            default -> Comparator.comparing(News::getPublishedAt, Comparator.nullsLast(LocalDateTime::compareTo));
        };
        
        return "desc".equalsIgnoreCase(sortOrder) ? comparator.reversed() : comparator;
    }
}
