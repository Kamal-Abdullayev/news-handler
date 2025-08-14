package com.example.demo.demo_project.service;

import com.example.demo.demo_project.constants.ProjectHelperConstant;
import com.example.demo.demo_project.dto.*;
import com.example.demo.demo_project.entity.News;
import com.example.demo.demo_project.repository.NewsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class NewsExportService {
    private final NewsRepository newsRepository;
    private final NewsAnalyticsService newsAnalyticsService;
    private final ProjectHelperConstant projectHelperConstant;

    /**
     * Export news data as JSON
     */
    public NewsExportDto exportNewsAsJson(NewsExportRequestDto exportRequest) {
        log.info("Exporting news as JSON with criteria: {}", exportRequest);
        
        List<News> newsList = getFilteredNews(exportRequest);
        String jsonContent = convertToJson(newsList);
        
        return new NewsExportDto(
                "news_export_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".json",
                "application/json",
                jsonContent.getBytes(),
                newsList.size(),
                LocalDateTime.now()
        );
    }

    /**
     * Export news data as CSV
     */
    public NewsExportDto exportNewsAsCsv(NewsExportRequestDto exportRequest) {
        log.info("Exporting news as CSV with criteria: {}", exportRequest);
        
        List<News> newsList = getFilteredNews(exportRequest);
        String csvContent = convertToCsv(newsList);
        
        return new NewsExportDto(
                "news_export_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv",
                "text/csv",
                csvContent.getBytes(),
                newsList.size(),
                LocalDateTime.now()
        );
    }

    /**
     * Generate a comprehensive news report
     */
    public NewsExportDto generateNewsReport(NewsReportRequestDto reportRequest) {
        log.info("Generating news report with criteria: {}", reportRequest);
        
        List<News> newsList = getFilteredNews(new NewsExportRequestDto(
                reportRequest.startDate(),
                reportRequest.endDate(),
                reportRequest.source(),
                reportRequest.author(),
                reportRequest.limit()
        ));
        
        String reportContent = generateReportContent(newsList, reportRequest);
        
        return new NewsExportDto(
                "news_report_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".txt",
                "text/plain",
                reportContent.getBytes(),
                newsList.size(),
                LocalDateTime.now()
        );
    }

    /**
     * Export news statistics
     */
    public NewsExportDto exportNewsStatistics() {
        log.info("Exporting news statistics");
        
        NewsStatisticsDto statistics = newsAnalyticsService.getNewsStatistics();
        String statsContent = convertStatisticsToText(statistics);
        
        return new NewsExportDto(
                "news_statistics_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".txt",
                "text/plain",
                statsContent.getBytes(),
                1, // Single statistics file
                LocalDateTime.now()
        );
    }

    /**
     * Export trending topics
     */
    public NewsExportDto exportTrendingTopics(int days) {
        log.info("Exporting trending topics for the last {} days", days);
        
        List<TrendingTopicDto> trendingTopics = newsAnalyticsService.getTrendingTopics(days);
        String topicsContent = convertTrendingTopicsToText(trendingTopics, days);
        
        return new NewsExportDto(
                "trending_topics_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".txt",
                "text/plain",
                topicsContent.getBytes(),
                trendingTopics.size(),
                LocalDateTime.now()
        );
    }

    // Helper methods
    private List<News> getFilteredNews(NewsExportRequestDto exportRequest) {
        List<News> allNews = newsRepository.findAllNews(projectHelperConstant.getNewsQueryLimit());
        
        return allNews.stream()
                .filter(news -> matchesDateRange(news, exportRequest.startDate(), exportRequest.endDate()))
                .filter(news -> matchesSource(news, exportRequest.source()))
                .filter(news -> matchesAuthor(news, exportRequest.author()))
                .limit(exportRequest.limit())
                .collect(Collectors.toList());
    }

    private boolean matchesDateRange(News news, LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null && endDate == null) return true;
        if (news.getPublishedAt() == null) return false;
        
        if (startDate != null && news.getPublishedAt().isBefore(startDate)) return false;
        if (endDate != null && news.getPublishedAt().isAfter(endDate)) return false;
        
        return true;
    }

    private boolean matchesSource(News news, String source) {
        if (source == null || source.trim().isEmpty()) return true;
        return news.getSource() != null && news.getSource().getName().toLowerCase().contains(source.toLowerCase());
    }

    private boolean matchesAuthor(News news, String author) {
        if (author == null || author.trim().isEmpty()) return true;
        return news.getAuthor() != null && news.getAuthor().toLowerCase().contains(author.toLowerCase());
    }

    private String convertToJson(List<News> newsList) {
        StringBuilder json = new StringBuilder("[\n");
        
        for (int i = 0; i < newsList.size(); i++) {
            News news = newsList.get(i);
            json.append("  {\n");
            json.append("    \"id\": ").append(news.getId()).append(",\n");
            json.append("    \"title\": \"").append(escapeJson(news.getTitle())).append("\",\n");
            json.append("    \"author\": \"").append(escapeJson(news.getAuthor())).append("\",\n");
            json.append("    \"description\": \"").append(escapeJson(news.getDescription())).append("\",\n");
            json.append("    \"url\": \"").append(escapeJson(news.getUrl())).append("\",\n");
            json.append("    \"publishedAt\": \"").append(news.getPublishedAt()).append("\",\n");
            json.append("    \"source\": \"").append(escapeJson(news.getSource() != null ? news.getSource().getName() : "")).append("\"\n");
            json.append("  }");
            
            if (i < newsList.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }
        
        json.append("]\n");
        return json.toString();
    }

    private String convertToCsv(List<News> newsList) {
        StringBuilder csv = new StringBuilder();
        
        // Header
        csv.append("ID,Title,Author,Description,URL,Published At,Source\n");
        
        // Data rows
        for (News news : newsList) {
            csv.append(news.getId()).append(",");
            csv.append(escapeCsv(news.getTitle())).append(",");
            csv.append(escapeCsv(news.getAuthor())).append(",");
            csv.append(escapeCsv(news.getDescription())).append(",");
            csv.append(escapeCsv(news.getUrl())).append(",");
            csv.append(news.getPublishedAt()).append(",");
            csv.append(escapeCsv(news.getSource() != null ? news.getSource().getName() : "")).append("\n");
        }
        
        return csv.toString();
    }

    private String generateReportContent(List<News> newsList, NewsReportRequestDto reportRequest) {
        StringBuilder report = new StringBuilder();
        
        report.append("NEWS REPORT\n");
        report.append("Generated on: ").append(LocalDateTime.now()).append("\n");
        report.append("Report Type: ").append(reportRequest.reportType()).append("\n");
        report.append("Date Range: ").append(reportRequest.startDate()).append(" to ").append(reportRequest.endDate()).append("\n");
        report.append("Total News Items: ").append(newsList.size()).append("\n\n");
        
        // Summary statistics
        Map<String, Long> sourceStats = newsList.stream()
                .collect(Collectors.groupingBy(
                        news -> news.getSource() != null ? news.getSource().getName() : "Unknown",
                        Collectors.counting()
                ));
        
        report.append("SOURCE STATISTICS:\n");
        sourceStats.forEach((source, count) -> 
                report.append("  ").append(source).append(": ").append(count).append(" articles\n"));
        
        report.append("\nDETAILED NEWS LIST:\n");
        report.append("=".repeat(80)).append("\n");
        
        for (News news : newsList) {
            report.append("Title: ").append(news.getTitle()).append("\n");
            report.append("Author: ").append(news.getAuthor()).append("\n");
            report.append("Source: ").append(news.getSource() != null ? news.getSource().getName() : "Unknown").append("\n");
            report.append("Published: ").append(news.getPublishedAt()).append("\n");
            report.append("Description: ").append(news.getDescription()).append("\n");
            report.append("-".repeat(80)).append("\n");
        }
        
        return report.toString();
    }

    private String convertStatisticsToText(NewsStatisticsDto statistics) {
        StringBuilder stats = new StringBuilder();
        
        stats.append("NEWS STATISTICS REPORT\n");
        stats.append("Generated on: ").append(statistics.generatedAt()).append("\n\n");
        
        stats.append("OVERVIEW:\n");
        stats.append("  Total News Items: ").append(statistics.totalNews()).append("\n");
        stats.append("  Date Range: ").append(statistics.oldestNewsDate()).append(" to ").append(statistics.newestNewsDate()).append("\n");
        stats.append("  Average Content Length: ").append(String.format("%.2f", statistics.averageContentLength())).append(" characters\n\n");
        
        stats.append("SOURCE STATISTICS:\n");
        statistics.sourceStatistics().forEach((source, count) -> 
                stats.append("  ").append(source).append(": ").append(count).append(" articles\n"));
        
        stats.append("\nTOP AUTHORS:\n");
        statistics.authorStatistics().entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .forEach(entry -> 
                        stats.append("  ").append(entry.getKey()).append(": ").append(entry.getValue()).append(" articles\n"));
        
        return stats.toString();
    }

    private String convertTrendingTopicsToText(List<TrendingTopicDto> trendingTopics, int days) {
        StringBuilder topics = new StringBuilder();
        
        topics.append("TRENDING TOPICS REPORT\n");
        topics.append("Generated on: ").append(LocalDateTime.now()).append("\n");
        topics.append("Analysis Period: Last ").append(days).append(" days\n\n");
        
        topics.append("TOP TRENDING TOPICS:\n");
        topics.append("=".repeat(50)).append("\n");
        
        for (int i = 0; i < trendingTopics.size(); i++) {
            TrendingTopicDto topic = trendingTopics.get(i);
            topics.append(i + 1).append(". ").append(topic.topic()).append("\n");
            topics.append("   Frequency: ").append(topic.frequency()).append(" mentions\n");
            topics.append("   Trend Score: ").append(String.format("%.2f", topic.trendScore())).append("%\n\n");
        }
        
        return topics.toString();
    }

    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }

    private String escapeCsv(String text) {
        if (text == null) return "";
        if (text.contains(",") || text.contains("\"") || text.contains("\n")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }
}
